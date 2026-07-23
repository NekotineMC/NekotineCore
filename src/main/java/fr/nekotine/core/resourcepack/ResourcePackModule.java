package fr.nekotine.core.resourcepack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.nekotine.core.module.IPluginModule;
import fr.nekotine.core.util.EventUtil;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;

public class ResourcePackModule implements IPluginModule, Listener {

	private List<ResourcePackInfoLike> mandatoryResourcePacks = new ArrayList<>();
	
	private Component mandatoryMessage;
	
	public ResourcePackModule() {
		EventUtil.register(this);
	}
	
	@Override
	public void unload() {
		// TODO Auto-generated method stub
		
	}
	
	public void addMandatoryMessage(Component message) {
		this.mandatoryMessage = message;
	}
	
	public void addMandatoryResourcePack(ResourcePackInfoLike resourcePack) {
		mandatoryResourcePacks.add(resourcePack);
	}
	
	@EventHandler
	public void onPlayerJoined(PlayerJoinEvent evt) {
		if (mandatoryResourcePacks.size() <= 0) {
			return;
		}
		var request = ResourcePackRequest.resourcePackRequest();
		request.packs(mandatoryResourcePacks);
		request.required(true);
		if (mandatoryMessage != null) {
			request.prompt(mandatoryMessage);
		}
		evt.getPlayer().sendResourcePacks(request.build());
	}
	
}
