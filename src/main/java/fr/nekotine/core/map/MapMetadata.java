package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.map.save.IMapSaver;

@SerializableAs("MapIdentifier")
public class MapMetadata implements ConfigurationSerializable{
	
	private Class<?> type;

	private String name;
	
	private String displayName;
	
	private String description;
	
	private Material icon;
	
	private IMapSaver saver;
	
	public MapMetadata() {}
	
	public MapMetadata(@NotNull Class<?> type, @NotNull String name, String displayName, String description, Material icon) {
		super();
		this.type = type;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.icon = icon;
	}
	
	public MapMetadata(@NotNull Class<?> type, @NotNull String name, String displayName, String description, Material icon,
			@NotNull IMapSaver saver) {
		super();
		this.type = type;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.icon = icon;
		this.saver = saver;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("type", type.getName());
		map.put("name", name);
		map.put("displayName", displayName);
		map.put("description", description);
		map.put("icon", icon != null ? icon.toString() : null);
		return map;
	}

	public static MapMetadata deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return new MapMetadata(
				Class.forName((String)map.get("type")),
				(String)map.get("name"),
				(String)map.get("displayName"),
				(String)map.get("description"),
				map.containsKey("icon")?Material.valueOf((String) map.get("icon")):null,
				null);
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Material getIcon() {
		return icon;
	}

	public void setIcon(Material icon) {
		this.icon = icon;
	}

	public IMapSaver getSaver() {
		return saver;
	}

	public void setSaver(IMapSaver saver) {
		this.saver = saver;
	}
	
	public void updateWith(MapMetadata id) {
		type = id.type;
		name = id.name;
		displayName = id.displayName;
		description = id.description;
		icon = id.icon;
		saver = id.saver;
	}
	
}