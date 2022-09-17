package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

@SerializableAs("MapIdentifier")
public record MapIdentifier(
		MapTypeIdentifier type,
		String name,
		String displayName,
		String description,
		Material icon
		) implements ConfigurationSerializable{
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("type", type.getId());
		map.put("name", name);
		map.put("displayName", displayName);
		map.put("description", description);
		map.put("icon", icon);
		return map;
	}

	public static MapIdentifier deserialize(Map<String, Object> map) {
		return new MapIdentifier(
				MapModule.getMapTypeById((String) map.get("type")),
				(String)map.get("name"),
				(String)map.get("displayName"),
				(String)map.get("description"),
				Material.valueOf((String) map.get("icon")));
	}
}