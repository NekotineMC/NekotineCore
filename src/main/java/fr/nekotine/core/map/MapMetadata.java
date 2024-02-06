package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

@SerializableAs("MapMetadata")
public class MapMetadata implements ConfigurationSerializable{
	
	private @Nonnull String name;
	
	private @Nullable Component displayName;
	
	private @Nullable Component description;
	
	private @Nullable Material icon;
	
	public MapMetadata() {}
	
	public MapMetadata(@Nonnull String name, @Nullable Component displayName, @Nullable Component description, @Nullable Material icon) {
		this.displayName = displayName;
		this.description = description;
		this.icon = icon;
	}
	
	@Override
	public @Nonnull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		if (displayName != null) {
			map.put("displayName", JSONComponentSerializer.json().serialize(displayName));
		}
		if (description != null) {
			map.put("description", JSONComponentSerializer.json().serialize(description));
		}
		if (icon != null) {
			map.put("icon", icon.toString());
		}
		return map;
	}

	public static MapMetadata deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return new MapMetadata((String)map.get("name"),
				map.containsKey("displayName")?JSONComponentSerializer.json().deserialize((String)map.get("displayName")):Component.text(""),
				map.containsKey("description")?JSONComponentSerializer.json().deserialize((String)map.get("description")):Component.text(""),
				map.containsKey("icon")?Material.valueOf((String) map.get("icon")):null);
	}

	public @Nullable Component getDisplayName() {
		return displayName;
	}

	public void setDisplayName(@Nullable Component displayName) {
		this.displayName = displayName;
	}

	public @Nullable Component getDescription() {
		return description;
	}

	public void setDescription(@Nullable Component description) {
		this.description = description;
	}

	public @Nullable Material getIcon() {
		return icon;
	}

	public void setIcon(@Nullable Material icon) {
		this.icon = icon;
	}
	
}