package fr.nekotine.botstesthost.it;

import static fr.nekotine.botstesthost.it.BotLifecycleIT.cleanUp;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.roster;
import static fr.nekotine.botstesthost.it.BotLifecycleIT.spawnLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.nekotine.botstesthost.it.BotLifecycleIT.LogCapture;
import fr.nekotine.core.bot.BotRoster;
import fr.nekotine.core.bot.BotsUnavailableException;
import fr.nekotine.core.bot.internal.BotSelfTest;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Story 1.5: the roster's startup self-test resolves every NMS accessor of the
 * internal classes, and a failure turns bots off loudly without stopping the
 * host.
 */
class BotSelfTestIT {

	private static final Pattern PASS_LINE = Pattern
			.compile("Bots self-test passed: (\\d+) accessors in (\\d+) classes");

	private static final String FAULT = "net.minecraft.server.MinecraftServer#injectedByBotSelfTestIT()V";

	private static final ClassDesc MINECRAFT_SERVER = ClassDesc.of("net.minecraft.server.MinecraftServer");

	private static final ClassDesc VEC3 = ClassDesc.of("net.minecraft.world.phys.Vec3");

	@Test
	void healthyRosterPasses() throws Exception {
		try (var logs = new LogCapture()) {
			var roster = PaperServer.onMain(BotRoster::new);
			try {
				assertTrue(roster.isAvailable(), "a healthy roster is unavailable: " + logs.lines);
				var matcher = logs.lines.stream().map(PASS_LINE::matcher).filter(m -> m.find()).findFirst()
						.orElseThrow(() -> new AssertionError("no pass line logged: " + logs.lines));
				assertTrue(Integer.parseInt(matcher.group(1)) > 0, "no accessor checked: " + matcher.group());
				assertTrue(Integer.parseInt(matcher.group(2)) >= 2, "fewer than 2 classes read: " + matcher.group());
				assertEquals(List.of(), PaperServer.onMain(roster::list));
			} finally {
				PaperServer.onMain(() -> {
					roster.unload();
					return null;
				});
			}
		}
		assertTrue(PaperServer.onMain(() -> roster().isAvailable()), "the host's roster is unavailable");
	}

	@Test
	void injectedFaultDisablesOnlyThatRoster() throws Exception {
		var hostRoster = roster();
		try (var logs = new LogCapture()) {
			BotSelfTest.injectFault(FAULT);
			var broken = PaperServer.onMain(BotRoster::new);
			try {
				assertFalse(broken.isAvailable(), "a roster with an injected fault is available");
				var errors = logs.lines.stream().filter(l -> l.contains(FAULT)).toList();
				assertEquals(1, errors.size(), "expected one error naming the fault: " + logs.lines);
				assertTrue(errors.getFirst().contains("disabled"), "error does not say bots are disabled: " + errors);
				assertTrue(logs.lines.stream().noneMatch(l -> PASS_LINE.matcher(l).find()), "pass line logged");
				var location = spawnLocation();
				var hostBot = PaperServer.onMain(() -> hostRoster.spawn("Bot_SelfTest", location));
				PaperServer.onMain(() -> {
					assertUnavailable(() -> broken.spawn("Bot_Broken", location));
					assertUnavailable(() -> broken.remove(hostBot));
					assertUnavailable(broken::list);
					assertTrue(PaperServer.plugin().isEnabled(), "the host was disabled");
					assertEquals(List.of(hostBot), hostRoster.list(), "the host's roster stopped working");
					assertTrue(hostBot.player().isOnline());
					return null;
				});
			} finally {
				PaperServer.onMain(() -> {
					broken.unload();
					return null;
				});
				cleanUp(hostRoster);
			}
		}
		// The seam is consumed by one run.
		var next = PaperServer.onMain(BotRoster::new);
		try {
			assertTrue(next.isAvailable(), "the injected fault outlived its run");
		} finally {
			PaperServer.onMain(() -> {
				next.unload();
				return null;
			});
		}
	}

	/** Builds a class whose static {@code call()} runs {@code body}. */
	private static byte[] synthesize(String simpleName, Consumer<CodeBuilder> body) {
		// Never loaded, only parsed: no stack maps needed.
		return ClassFile.of(ClassFile.StackMapsOption.DROP_STACK_MAPS)
				.build(ClassDesc.of("fr.nekotine.botstesthost.it." + simpleName), clazz -> clazz.withMethodBody("call",
						MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, code -> {
							body.accept(code);
							code.return_();
						}));
	}

	@Test
	void validCallsAreNotReported() throws Exception {
		var bytes = synthesize("SynthesizedValidCaller", code -> {
			code.invokestatic(MINECRAFT_SERVER, "getServer", MethodTypeDesc.of(MINECRAFT_SERVER));
			code.pop();
			code.getstatic(VEC3, "ZERO", VEC3);
			code.invokevirtual(VEC3, "length", MethodTypeDesc.of(ConstantDescs.CD_double));
			code.pop2();
		});
		assertEquals(List.of(), BotSelfTest.check(bytes, BotRoster.class.getClassLoader()));
	}

	@Test
	void missingMembersAndClassesAreReported() throws Exception {
		var bytes = synthesize("SynthesizedMissingCaller", code -> {
			code.invokestatic(MINECRAFT_SERVER, "noSuchMethod", MethodTypeDesc.of(ConstantDescs.CD_void));
			code.getstatic(MINECRAFT_SERVER, "noSuchField", ConstantDescs.CD_int);
			code.pop();
			code.invokestatic(ClassDesc.of("net.minecraft.NoSuchBotsClass"), "anything",
					MethodTypeDesc.of(ConstantDescs.CD_void));
			// Existing name, wrong descriptor.
			code.invokestatic(MINECRAFT_SERVER, "getServer", MethodTypeDesc.of(ConstantDescs.CD_void));
			// No no-arg constructor.
			code.new_(MINECRAFT_SERVER);
			code.dup();
			code.invokespecial(MINECRAFT_SERVER, "<init>", MethodTypeDesc.of(ConstantDescs.CD_void));
			code.pop();
			// Existing field, wrong type.
			code.getstatic(VEC3, "ZERO", ConstantDescs.CD_int);
			code.pop();
		});
		var broken = BotSelfTest.check(bytes, BotRoster.class.getClassLoader());
		assertEquals(List
				.of("net.minecraft.server.MinecraftServer#noSuchMethod()V",
						"net.minecraft.server.MinecraftServer#noSuchField:I", "net.minecraft.NoSuchBotsClass",
						"net.minecraft.server.MinecraftServer#getServer()V",
						"net.minecraft.server.MinecraftServer#<init>()V", "net.minecraft.world.phys.Vec3#ZERO:I")
				.stream().sorted().toList(), broken.stream().sorted().toList());
	}

	@Test
	void kindAndAccessMismatchesAreReported() throws Exception {
		var packagePrivate = packagePrivateStaticMethod();
		var owner = ClassDesc.of(packagePrivate.getDeclaringClass().getName());
		var descriptor = MethodTypeDesc
				.ofDescriptor(MethodType.methodType(packagePrivate.getReturnType(), packagePrivate.getParameterTypes())
						.toMethodDescriptorString());
		var bytes = synthesize("SynthesizedKindCaller", code -> {
			code.aconst_null();
			code.invokestatic(VEC3, "length", MethodTypeDesc.of(ConstantDescs.CD_double));
			code.pop2();
			code.aconst_null();
			code.invokevirtual(MINECRAFT_SERVER, "getServer", MethodTypeDesc.of(MINECRAFT_SERVER));
			code.pop();
			code.aconst_null();
			code.getfield(ClassDesc.of(ServerGamePacketListenerImpl.class.getName()), "awaitingTeleport",
					ConstantDescs.CD_int);
			code.pop();
			for (var parameter : descriptor.parameterList()) {
				switch (TypeKind.from(parameter)) {
					case LONG -> code.lconst_0();
					case FLOAT -> code.fconst_0();
					case DOUBLE -> code.dconst_0();
					case REFERENCE -> code.aconst_null();
					default -> code.iconst_0();
				}
			}
			code.invokestatic(owner, packagePrivate.getName(), descriptor);
			var returned = TypeKind.from(descriptor.returnType());
			if (returned.slotSize() == 2) {
				code.pop2();
			} else if (returned.slotSize() == 1) {
				code.pop();
			}
		});
		var broken = BotSelfTest.check(bytes, BotRoster.class.getClassLoader());
		assertEquals(
				List.of("net.minecraft.world.phys.Vec3#length()D (is instance, used as static)",
						"net.minecraft.server.MinecraftServer#getServer()Lnet/minecraft/server/MinecraftServer; "
								+ "(is static, used as instance)",
						"net.minecraft.server.network.ServerGamePacketListenerImpl#awaitingTeleport:I (private)",
						packagePrivate.getDeclaringClass().getName() + "#" + packagePrivate.getName()
								+ descriptor.descriptorString() + " (package-private)")
						.stream().sorted().toList(),
				broken.stream().sorted().toList());
	}

	/**
	 * A package-private static method of a well-known NMS class, found by
	 * reflection (Paper widens most NMS members to public).
	 */
	private static Method packagePrivateStaticMethod() {
		for (var type : List.of(Entity.class, MinecraftServer.class, ServerPlayer.class,
				ServerGamePacketListenerImpl.class, Connection.class, PlayerList.class)) {
			for (var method : type.getDeclaredMethods()) {
				int modifiers = method.getModifiers();
				if (Modifier.isStatic(modifiers) && !Modifier.isPublic(modifiers) && !Modifier.isProtected(modifiers)
						&& !Modifier.isPrivate(modifiers) && !method.isSynthetic()) {
					return method;
				}
			}
		}
		throw new AssertionError("no package-private static method found in the probed NMS classes");
	}

	@Test
	void noInternalClassIsAFailure() throws Exception {
		var loader = BotRoster.class.getClassLoader();
		var empty = Files.createTempDirectory("bots-self-test-empty");
		try {
			var result = BotSelfTest.run(empty, loader);
			assertFalse(result.passed(), "an empty code source passed");
			assertEquals(0, result.classes());
			var missing = BotSelfTest.run(empty.resolve("missing.jar"), loader);
			assertFalse(missing.passed(), "an unreadable code source passed");
		} finally {
			Files.deleteIfExists(empty);
		}
	}

	private static void assertUnavailable(Executable call) {
		var error = assertThrows(BotsUnavailableException.class, call);
		assertTrue(error.getMessage().startsWith("Bots unavailable"), error.getMessage());
		assertTrue(error instanceof IllegalStateException);
	}
}
