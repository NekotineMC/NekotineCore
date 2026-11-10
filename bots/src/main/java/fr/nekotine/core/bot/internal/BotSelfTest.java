package fr.nekotine.core.bot.internal;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.classfile.constantpool.InterfaceMethodRefEntry;
import java.lang.classfile.constantpool.MemberRefEntry;
import java.lang.classfile.constantpool.MethodHandleEntry;
import java.lang.classfile.constantpool.MethodRefEntry;
import java.lang.invoke.MethodHandleInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.classfile.constantpool.PoolEntry;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
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
	 * references, with {@code loader}, without loading the class itself. A resolved
	 * member must also match how the class uses it (static or instance, interface
	 * or class owner) and be accessible from it.
	 *
	 * @return the broken accessors, in constant-pool order, each with its reason
	 *         unless it is simply missing; a missing class is reported once, not
	 *         again for its members
	 * @throws IllegalArgumentException
	 *             if the bytes are not a class file
	 */
	public static List<String> check(byte[] classBytes, ClassLoader loader) {
		return check(classBytes, loader, new HashSet<>());
	}

	private static List<String> check(byte[] classBytes, ClassLoader loader, Set<String> counted) {
		var model = ClassFile.of().parse(classBytes);
		var caller = new Caller(model, loader);
		var uses = staticUses(model);
		var checked = new HashSet<String>();
		var broken = new ArrayList<String>();
		for (PoolEntry entry : model.constantPool()) {
			String accessor;
			String problem;
			if (entry instanceof MemberRefEntry member) {
				var owner = elementName(member.owner().asInternalName());
				if (owner == null || ignored(owner)) {
					continue;
				}
				accessor = accessor(member);
				counted.add(accessor);
				if (!checked.add(accessor)) {
					continue;
				}
				var type = load(owner, loader);
				if (type == null) {
					// Reported once, by its class entry.
					continue;
				}
				problem = checkMember(type, member, uses.getOrDefault(accessor, Set.of()), caller);
			} else if (entry instanceof ClassEntry type) {
				var owner = elementName(type.asInternalName());
				if (owner == null || ignored(owner)) {
					continue;
				}
				accessor = dotted(owner);
				counted.add(accessor);
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

	/** {@code owner#name(descriptor)} or {@code owner#name:descriptor}. */
	private static String accessor(MemberRefEntry member) {
		var internal = member.owner().asInternalName();
		var owner = elementName(internal);
		return dotted(owner == null ? internal : owner) + "#" + member.name().stringValue()
				+ (member instanceof FieldRefEntry ? ":" : "") + member.type().stringValue();
	}

	/**
	 * How the class uses each member ref, from its field and invoke instructions
	 * and its method handles: {@code true} for a static use, {@code false} for an
	 * instance one.
	 */
	private static Map<String, Set<Boolean>> staticUses(ClassModel model) {
		var uses = new HashMap<String, Set<Boolean>>();
		for (var method : model.methods()) {
			var code = method.code();
			if (code.isEmpty()) {
				continue;
			}
			for (var element : code.get()) {
				if (element instanceof FieldInstruction instruction) {
					var opcode = instruction.opcode();
					uses.computeIfAbsent(accessor(instruction.field()), _ -> new HashSet<>())
							.add(opcode == Opcode.GETSTATIC || opcode == Opcode.PUTSTATIC);
				} else if (element instanceof InvokeInstruction instruction) {
					uses.computeIfAbsent(accessor(instruction.method()), _ -> new HashSet<>())
							.add(instruction.opcode() == Opcode.INVOKESTATIC);
				}
			}
		}
		for (var entry : model.constantPool()) {
			if (entry instanceof MethodHandleEntry handle) {
				int kind = handle.kind();
				boolean isStatic = kind == MethodHandleInfo.REF_getStatic || kind == MethodHandleInfo.REF_putStatic
						|| kind == MethodHandleInfo.REF_invokeStatic;
				uses.computeIfAbsent(accessor(handle.reference()), _ -> new HashSet<>()).add(isStatic);
			}
		}
		return uses;
	}

	/**
	 * @return why the member is broken, {@code "missing"}, or null if it links from
	 *         the caller as used
	 */
	private static String checkMember(Class<?> type, MemberRefEntry ref, Set<Boolean> staticUses, Caller caller) {
		var name = ref.name().stringValue();
		var descriptor = ref.type().stringValue();
		try {
			if (ref instanceof MethodRefEntry && type.isInterface()) {
				return "owner is an interface, referenced as a class";
			}
			if (ref instanceof InterfaceMethodRefEntry && !type.isInterface()) {
				return "owner is a class, referenced as an interface";
			}
			Member member = ref instanceof FieldRefEntry
					? resolveField(type, name, descriptor)
					: resolveMethod(type, name, descriptor);
			if (member == null) {
				return "missing";
			}
			if (!name.equals("<init>")) {
				boolean isStatic = Modifier.isStatic(member.getModifiers());
				if (isStatic && staticUses.contains(false)) {
					return "is static, used as instance";
				}
				if (!isStatic && staticUses.contains(true)) {
					return "is instance, used as static";
				}
			}
			return caller.access(member);
		} catch (LinkageError e) {
			return e.toString();
		}
	}

	/** The class being checked, as the JVM sees it for access checks. */
	private static final class Caller {

		private final String name;

		private final String packageName;

		private final String superName;

		private final ClassLoader loader;

		private Class<?> superclass;

		private boolean superLoaded;

		Caller(ClassModel model, ClassLoader loader) {
			name = dotted(model.thisClass().asInternalName());
			int dot = name.lastIndexOf('.');
			packageName = dot < 0 ? "" : name.substring(0, dot);
			superName = model.superclass().map(c -> dotted(c.asInternalName())).orElse(null);
			this.loader = loader;
		}

		/**
		 * @return why {@code member} is not accessible from this class, or null:
		 *         public, protected from a subclass, same package, or private to this
		 *         class
		 */
		String access(Member member) {
			int modifiers = member.getModifiers();
			var declaring = member.getDeclaringClass();
			if (Modifier.isPublic(modifiers)) {
				return null;
			}
			if (Modifier.isPrivate(modifiers)) {
				return declaring.getName().equals(name) ? null : "private";
			}
			if (declaring.getPackageName().equals(packageName)) {
				return null;
			}
			if (Modifier.isProtected(modifiers)) {
				var sup = superclass();
				return sup != null && declaring.isAssignableFrom(sup) ? null : "protected, caller is not a subclass";
			}
			return "package-private";
		}

		private Class<?> superclass() {
			if (!superLoaded) {
				superLoaded = true;
				if (superName != null) {
					try {
						superclass = Class.forName(superName, false, loader);
					} catch (ClassNotFoundException | LinkageError e) {
						superclass = null;
					}
				}
			}
			return superclass;
		}
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
	 *
	 * @return the field, or null if missing
	 */
	private static Field resolveField(Class<?> type, String name, String descriptor) {
		for (var candidate : hierarchy(type)) {
			for (var field : candidate.getDeclaredFields()) {
				if (field.getName().equals(name) && field.getType().descriptorString().equals(descriptor)) {
					return field;
				}
			}
		}
		return null;
	}

	/**
	 * JVM method resolution: the owner and its superclasses, then its interfaces
	 * (and {@link Object} for an interface owner); constructors on the owner only.
	 *
	 * @return the method or constructor, or null if missing
	 */
	private static Member resolveMethod(Class<?> type, String name, String descriptor) {
		if (name.equals("<init>")) {
			for (var constructor : type.getDeclaredConstructors()) {
				if (descriptor(constructor.getParameterTypes(), void.class).equals(descriptor)) {
					return constructor;
				}
			}
			return null;
		}
		var candidates = hierarchy(type);
		if (type.isInterface()) {
			candidates.add(Object.class);
		}
		for (var candidate : candidates) {
			for (var method : candidate.getDeclaredMethods()) {
				if (method.getName().equals(name)
						&& descriptor(method.getParameterTypes(), method.getReturnType()).equals(descriptor)) {
					return method;
				}
			}
		}
		return null;
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
