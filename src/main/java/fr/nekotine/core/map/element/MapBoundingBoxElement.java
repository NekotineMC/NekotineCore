package fr.nekotine.core.map.element;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Une élément de carte qui représente un point désigné par une valeur sur trois axes (x,y,z).
 * La précision sur chaque axe est celle d'un double.
 * @author XxGoldenbluexX
 *
 */
public class MapBoundingBoxElement implements ConfigurationSerializable{

	// ---------------------- Serialization

		public static MapBoundingBoxElement deserialize(Map<String, Object> map) {
			var corner1 = (Vector) 			map.get("corner1");
			var corner2 = (Vector) 			map.get("corner2");
			return new MapBoundingBoxElement(corner1, corner2);
		}

		@Override
		public @NotNull Map<String, Object> serialize() {
			var map = new HashMap<String, Object>();
			map.put("corner1", boundingBox.getMin());
			map.put("corner2", boundingBox.getMax());
			return map;
		}

		// ----------------------
		
		private BoundingBox boundingBox = new BoundingBox();
		
		public MapBoundingBoxElement() {}
		
		public MapBoundingBoxElement(Vector corner1, Vector corner2) {
			boundingBox.resize(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
		}
		
		public BoundingBox get() {
			return boundingBox;
		}
	
}
