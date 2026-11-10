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
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
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

	@Test
	void missingMembersAndClassesAreReported() throws Exception {
		var bytes = ClassFile.of().build(ClassDesc.of("fr.nekotine.botstesthost.it.SynthesizedBotsCaller"), clazz -> {
			clazz.withMethodBody("call", MethodTypeDesc.of(ConstantDescs.CD_void),
					ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, code -> {
						code.invokestatic(MINECRAFT_SERVER, "noSuchMethod", MethodTypeDesc.of(ConstantDescs.CD_void));
						code.invokestatic(MINECRAFT_SERVER, "getServer", MethodTypeDesc.of(MINECRAFT_SERVER));
						code.pop();
						code.getstatic(MINECRAFT_SERVER, "noSuchField", ConstantDescs.CD_int);
						code.pop();
						code.invokestatic(ClassDesc.of("net.minecraft.NoSuchBotsClass"), "anything",
								MethodTypeDesc.of(ConstantDescs.CD_void));
						code.return_();
					});
		});
		var broken = BotSelfTest.check(bytes, BotRoster.class.getClassLoader());
		assertTrue(broken.contains("net.minecraft.server.MinecraftServer#noSuchMethod()V"),
				"missing method not reported: " + broken);
		assertTrue(broken.contains("net.minecraft.server.MinecraftServer#noSuchField:I"),
				"missing field not reported: " + broken);
		assertTrue(broken.contains("net.minecraft.NoSuchBotsClass"), "missing class not reported: " + broken);
		assertTrue(broken.stream().noneMatch(b -> b.contains("getServer")), "existing method reported: " + broken);
		assertEquals(3, broken.stream().filter(b -> !b.startsWith("net.minecraft.NoSuchBotsClass#")).count(),
				"unexpected reports: " + broken);
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
