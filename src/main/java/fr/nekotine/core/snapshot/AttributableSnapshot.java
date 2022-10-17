package fr.nekotine.core.snapshot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

public class AttributableSnapshot implements Snapshot<Attributable> {

	private Map<Attribute, Snapshot<AttributeInstance>> attributes;
	
	@Override
	public Snapshot<Attributable> snapshot(Attributable item) {
		attributes = new HashMap<>();
		for (var attr : Attribute.values()) {
			var inst = item.getAttribute(attr);
			if (inst != null) {
				var snap = new AttributeInstanceSnapshot().snapshot(inst);
				attributes.put(attr, snap);
			}
		}
		return this;
	}

	@Override
	public Snapshot<Attributable> deepSnapshot(Attributable item) {
		attributes = new HashMap<>();
		for (var attr : Attribute.values()) {
			var inst = item.getAttribute(attr);
			if (inst != null) {
				var snap = new AttributeInstanceSnapshot();
				snap.deepSnapshot(inst);
				attributes.put(attr, snap);
			}
		}
		return this;
	}

	@Override
	public void patch(Attributable item) {
		if (attributes != null) {
			for (var attr : attributes.keySet()) {
				var snap = attributes.get(attr);
				snap.patch(item.getAttribute(attr));
			}
		}
	}

}
