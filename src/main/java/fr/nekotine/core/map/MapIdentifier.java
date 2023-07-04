package fr.nekotine.core.map;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.map.save.IMapSaver;

@SerializableAs("MapIdentifier")
public class MapIdentifier implements ConfigurationSerializable{
	
	private Class<?> type;

	private String name;
	
	private String displayName;
	
	private String description;
	
	private Material icon;
	
	private Class<? extends IMapSaver> saver;
	
	public MapIdentifier() {}
	
	public MapIdentifier(Class<?> type, String name, String displayName, String description, Material icon) {
		super();
		this.type = type;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.icon = icon;
	}
	
	public MapIdentifier(Class<?> type, String name, String displayName, String description, Material icon,
			Class<? extends IMapSaver> saver) {
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
		map.put("icon", icon.toString());
		return map;
	}

	public static MapIdentifier deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return new MapIdentifier(
				Class.forName((String)map.get("type")),
				(String)map.get("name"),
				(String)map.get("displayName"),
				(String)map.get("description"),
				Material.valueOf((String) map.get("icon")),null);
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

	public Class<? extends IMapSaver> getSaver() {
		return saver;
	}

	public void setSaver(Class<? extends IMapSaver> saver) {
		this.saver = saver;
	}
	
}