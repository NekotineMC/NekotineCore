package fr.nekotine.botstesthost.it;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.Test;

import fr.nekotine.botstesthost.BotsTestHost;
import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.ticking.TickingModule;
import fr.xxgoldenbluexx.paperjunitserver.PaperServer;

/** The host boots on a real server, with ProtocolLib, and hosts the tests. */
class BotsTestHostSmokeIT {

	@Test
	void hostAndProtocolLibEnabled() throws Exception {
		PaperServer.awaitTicks(5);
		assertTrue(PaperServer.onMain(() -> PaperServer.plugin().isEnabled()), "BotsTestHost is not enabled");
		assertTrue(PaperServer.onMain(() -> {
			var protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
			return protocolLib != null && protocolLib.isEnabled();
		}), "ProtocolLib is not enabled");
	}

	@Test
	void enableCompleted() throws Exception {
		// setupModules() only logs its exceptions: a registered, constructible Core
		// module shows the module scan ran.
		var ticking = PaperServer.onMain(() -> Ioc.resolve(TickingModule.class));
		assertNotNull(ticking, "TickingModule not registered: NekotinePlugin.setupModules did not complete");
	}

	@Test
	void loadedThroughHostClassLoader() throws Exception {
		var plugin = PaperServer.plugin();
		assertInstanceOf(BotsTestHost.class, plugin, "target plugin is not BotsTestHost");
		var pluginLoader = plugin.getClass().getClassLoader();
		assertSame(pluginLoader, getClass().getClassLoader().getParent(),
				"test class loader's parent is not BotsTestHost's plugin classloader");
		assertSame(pluginLoader, Ioc.class.getClassLoader(), "Core's Ioc is not loaded from BotsTestHost's jar");
		assertSame(plugin, PaperServer.onMain(() -> Ioc.resolve(BotsTestHost.class)),
				"BotsTestHost is not registered in Core's Ioc");
	}
}
