package fr.nekotine.core.bot.internal;

import java.lang.classfile.ClassFile;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.PoolEntry;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Startup self-test of the bots library: reads the bytecode of every class in
 * {@code fr.nekotine.core.bot.internal} (as resources, without loading them),
 * resolves every external class, method and field their constant pools
 * reference, the way the JVM would link them, then initializes those classes so
 * the verifier runs. A Paper update that renames or removes an NMS member shows
 * up here, at boot, instead of as a {@link LinkageError} at the first spawn or
 * tick.
 *
 * <p>
 * An accessor is a class, method or field reference whose owner is not under
 * {@code java/}, {@code javax/}, {@code jdk/}, {@code sun/} or
 * {@code fr/nekotine/}. Reflective accessors (none today) are not covered and
 * would need their own check here.
 *
 * <p>
 * Internal: hosts see the result through {@code BotRoster.isAvailable()}.
 * Internal classes must keep side-effect-free static initializers.
 */
public final class BotSelfTest {

	/** The internal package, as a resource path. */
	static final String PACKAGE = "fr/nekotine/core/bot/internal/";

	private static final List<String> IGNORED = List.of("java/", "javax/", "jdk/", "sun/", "fr/nekotine/");

	private static final AtomicReference<String> injectedFault = new AtomicReference<>();

	private BotSelfTest() {
	}

	/**
	 * Outcome of a run.
	 *
	 * @param accessors
	 *            distinct external accessors checked
	 * @param classes
	 *            internal classes read
	 * @param broken
	 *            every failure: broken accessors ({@code owner#name(descriptor)},
	 *            {@code owner#name:descriptor} or a class name) and unreadable or
	 *            uninitializable classes; empty when the run passed
	 */
	public record Result(int accessors, int classes, List<String> broken) {

		public Result {
			broken = List.copyOf(broken);
		}

		public boolean passed() {
			return broken.isEmpty();
		}
	}

	/**
	 * Test seam, internal: makes the next {@link #run} report {@code accessor} as
	 * broken. Consumed by that run only.
	 */
	public static void injectFault(String accessor) {
		injectedFault.set(accessor);
	}

	/**
	 * Runs the self-test on the internal classes of this class's code source (the
	 * jar or class directory it was loaded from), resolving with {@code loader}.
	 * Never throws: every failure is in the result.
	 */
	public static Result run(ClassLoader loader) {
		Path location;
		try {
			var source = BotSelfTest.class.getProtectionDomain().getCodeSource();
			if (source == null || source.getLocation() == null) {
				return withInjected(new Result(0, 0, List.of("no code source to read the internal classes from")));
			}
			location = Path.of(source.getLocation().toURI());
		} catch (URISyntaxException | RuntimeException e) {
			return withInjected(new Result(0, 0, List.of("unreadable code source: " + e)));
		}
		return run(location, loader);
	}

	/**
	 * Runs the self-test on the internal classes found in {@code location}, a jar
	 * or a class directory, resolving with {@code loader}. Never throws: every
	 * failure, including finding no internal class, is in the result. Consumes an
	 * injected fault.
	 */
	public static Result run(Path location, ClassLoader loader) {
		var broken = new LinkedHashSet<String>();
		var accessors = new HashSet<String>();
		Map<String, byte[]> classes;
		try {
			classes = readClasses(location);
		} catch (Exception e) {
			broken.add("cannot read the internal classes from " + location + ": " + e);
			classes = Map.of();
		}
		if (classes.isEmpty() && broken.isEmpty()) {
			broken.add("no internal class found in " + location);
		}
		for (var entry : classes.entrySet()) {
			try {
				broken.addAll(check(entry.getValue(), loader, accessors));
			} catch (Exception | LinkageError e) {
				broken.add(entry.getKey() + " (unreadable: " + e + ")");
			}
		}
		for (var name : classes.keySet()) {
			try {
				Class.forName(name, true, loader);
			} catch (Exception | LinkageError e) {
				broken.add(name + " (initialization failed: " + e + ")");
			}
		}
		return withInjected(new Result(accessors.size(), classes.size(), new ArrayList<>(broken)));
	}

	/**
	 * Resolves every external accessor the constant pool of {@code classBytes}
	 * references, with {@code loader}, without loading the class itself.
	 *
	 * @return the broken accessors, in constant-pool order
	 * @throws IllegalArgumentException
	 *             if the bytes are not a class file
	 */
	public static List<String> check(byte[] classBytes, ClassLoader loader) {
		return check(classBytes, loader, new HashSet<>());
	}

	private static List<String> check(byte[] classBytes, ClassLoader loader, Set<String> checked) {
		var model = ClassFile.of().parse(classBytes);
		var broken = new ArrayList<String>();
		for (PoolEntry entry : model.constantPool()) {
			String accessor;
			String problem;
			if (entry instanceof MemberRefEntry member) {
				var owner = elementName(member.owner().asInternalName());
				if (owner == null || ignored(owner)) {
					continue;
				}
				var name = member.name().stringValue();
				var descriptor = member.type().stringValue();
				boolean field = member instanceof FieldRefEntry;
				accessor = dotted(owner) + "#" + name + (field ? ":" : "") + descriptor;
				if (!checked.add(accessor)) {
					continue;
				}
				problem = field
						? resolveField(owner, name, descriptor, loader)
						: resolveMethod(owner, name, descriptor, loader);
			} else if (entry instanceof ClassEntry type) {
				var owner = elementName(type.asInternalName());
				if (owner == null || ignored(owner)) {
					continue;
				}
				accessor = dotted(owner);
				if (!checked.add(accessor)) {
					continue;
				}
				problem = load(owner, loader) == null ? "missing" : null;
			} else {
				continue;
			}
			if (problem != null) {
				broken.add(problem.equals("missing") ? accessor : accessor + " (" + problem + ")");
			}
		}
		return broken;
	}

	private static Result withInjected(Result result) {
		var fault = injectedFault.getAndSet(null);
		if (fault == null) {
			return result;
		}
		var broken = new ArrayList<>(result.broken());
		broken.add(fault);
		return new Result(result.accessors(), result.classes(), broken);
	}

	/** Internal classes by binary name, read as bytes, in name order. */
	private static Map<String, byte[]> readClasses(Path location) throws Exception {
		if (Files.isDirectory(location)) {
			return readPackage(location.resolve(PACKAGE));
		}
		if (!Files.isRegularFile(location)) {
			throw new FileNotFoundException(location.toString());
		}
		try (var jar = FileSystems.newFileSystem(location)) {
			return readPackage(jar.getPath(PACKAGE));
		}
	}

	private static Map<String, byte[]> readPackage(Path packageDir) throws Exception {
		var classes = new LinkedHashMap<String, byte[]>();
		if (!Files.isDirectory(packageDir)) {
			return classes;
		}
		List<Path> files;
		try (var listing = Files.list(packageDir)) {
			files = listing.filter(p -> p.getFileName().toString().endsWith(".class")).sorted()
					.collect(Collectors.toList());
		}
		for (var file : files) {
			var simple = file.getFileName().toString();
			var name = dotted(PACKAGE + simple.substring(0, simple.length() - ".class".length()));
			classes.put(name, Files.readAllBytes(file));
		}
		return classes;
	}

	/**
	 * @return the internal name of an array's element class, null for primitives
	 */
	private static String elementName(String internalName) {
		int dims = 0;
		while (dims < internalName.length() && internalName.charAt(dims) == '[') {
			dims++;
		}
		if (dims == 0) {
			return internalName;
		}
		if (internalName.charAt(dims) != 'L') {
			return null;
		}
		return internalName.substring(dims + 1, internalName.length() - 1);
	}

	private static boolean ignored(String internalName) {
		return IGNORED.stream().anyMatch(internalName::startsWith);
	}

	private static String dotted(String internalName) {
		return internalName.replace('/', '.');
	}

	private static Class<?> load(String internalName, ClassLoader loader) {
		try {
			return Class.forName(dotted(internalName), false, loader);
		} catch (ClassNotFoundException | LinkageError e) {
			return null;
		}
	}

	/**
	 * JVM field resolution, by name and type: the owner, its superclasses and its
	 * superinterfaces.
	 */
	private static String resolveField(String owner, String name, String descriptor, ClassLoader loader) {
		var type = load(owner, loader);
		if (type == null) {
			return "missing class " + dotted(owner);
		}
		try {
			for (var candidate : hierarchy(type)) {
				for (var field : candidate.getDeclaredFields()) {
					if (field.getName().equals(name) && field.getType().descriptorString().equals(descriptor)) {
						return null;
					}
				}
			}
		} catch (LinkageError e) {
			return e.toString();
		}
		return "missing";
	}

	/**
	 * JVM method resolution: the owner and its superclasses, then its interfaces
	 * (and {@link Object} for an interface owner); constructors on the owner only.
	 */
	private static String resolveMethod(String owner, String name, String descriptor, ClassLoader loader) {
		var type = load(owner, loader);
		if (type == null) {
			return "missing class " + dotted(owner);
		}
		try {
			if (name.equals("<init>")) {
				for (var constructor : type.getDeclaredConstructors()) {
					if (descriptor(constructor.getParameterTypes(), void.class).equals(descriptor)) {
						return null;
					}
				}
				return "missing";
			}
			var candidates = hierarchy(type);
			if (type.isInterface()) {
				candidates.add(Object.class);
			}
			for (var candidate : candidates) {
				for (var method : candidate.getDeclaredMethods()) {
					if (method.getName().equals(name)
							&& descriptor(method.getParameterTypes(), method.getReturnType()).equals(descriptor)) {
						return null;
					}
				}
			}
		} catch (LinkageError e) {
			return e.toString();
		}
		return "missing";
	}

	/** The type, its superclasses, then every superinterface, each once. */
	private static List<Class<?>> hierarchy(Class<?> type) {
		var result = new ArrayList<Class<?>>();
		for (var c = type; c != null; c = c.getSuperclass()) {
			result.add(c);
		}
		var queue = new ArrayDeque<Class<?>>(result);
		var seen = new HashSet<Class<?>>(result);
		while (!queue.isEmpty()) {
			for (var itf : queue.poll().getInterfaces()) {
				if (seen.add(itf)) {
					result.add(itf);
					queue.add(itf);
				}
			}
		}
		return result;
	}

	private static String descriptor(Class<?>[] parameters, Class<?> returnType) {
		var builder = new StringBuilder("(");
		for (var parameter : parameters) {
			builder.append(parameter.descriptorString());
		}
		return builder.append(')').append(returnType.descriptorString()).toString();
	}
}
