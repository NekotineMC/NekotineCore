package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

@SerializableAs("MapIdentifier")
public class MapMetadata implements ConfigurationSerializable{
	
	private Component displayName;
	
	private Component description;
	
	private Material icon;
	
	public MapMetadata() {}
	
	public MapMetadata(Component displayName, Component description, Material icon) {
		this.displayName = displayName;
		this.description = description;
		this.icon = icon;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("displayName", JSONComponentSerializer.json().serialize(displayName));
		map.put("description", JSONComponentSerializer.json().serialize(description));
		if (icon != null) {
			map.put("icon", icon.toString());
		}
		return map;
	}

	public static MapMetadata deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return new MapMetadata(
				JSONComponentSerializer.json().deserialize((String)map.get("displayName")),
				JSONComponentSerializer.json().deserialize((String)map.get("description")),
				map.containsKey("icon")?Material.valueOf((String) map.get("icon")):null);
	}

	public Component getDisplayName() {
		return displayName;
	}

	public void setDisplayName(Component displayName) {
		this.displayName = displayName;
	}

	public Component getDescription() {
		return description;
	}

	public void setDescription(Component description) {
		this.description = description;
	}

	public @Nullable Material getIcon() {
		return icon;
	}

	public void setIcon(@Nullable Material icon) {
		this.icon = icon;
	}
	
}