package fr.nekotine.core;

import java.net.URI;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NekotineCoreBootstrapper implements PluginBootstrap {

	@Override
	public void bootstrap(BootstrapContext context) {
	}

	public void withDatapack(BootstrapContext context, URI datapackUri, String packid) {
		context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(evt -> {
			try {
				evt.registrar().discoverPack(datapackUri, packid);
			} catch (Exception e) {
				context.getLogger()
						.error(Component.text(
								"Impossible de charger le datapack avec l'URI suivant: " + datapackUri.toString(),
								NamedTextColor.RED), e);
			}
		}));
	}

	public void withEmbedDatapack(BootstrapContext context, String resourcepath, String packid) {
		if (!resourcepath.startsWith("/")) {
			resourcepath = '/' + resourcepath;
		}
		final var finalpath = resourcepath;
		context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(evt -> {
			try {
				evt.registrar().discoverPack(this.getClass().getResource(finalpath).toURI(), packid);
			} catch (Exception e) {
				context.getLogger()
						.error(Component.text(
								"Impossible de charger le datapack contenu dans le jar au chemin suivant: " + finalpath,
								NamedTextColor.RED), e);
			}
		}));
	}

}
