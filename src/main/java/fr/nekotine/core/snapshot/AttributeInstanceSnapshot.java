package fr.nekotine.core.snapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

/**
 * Certains champs d"{@link fr.nekotine.core.snapshot.AttributeInstanceSnapshot AttributeInstanceSnapshot}
 * ne peuvent être écrits, notamment l'{@link org.bukkit.attribute.Attribute Attribute}
 * lié à cette instance.
 * Seule la base value et les modifiers sont sauvegardés.
 * @author XxGoldenbluexX
 *
 */
public class AttributeInstanceSnapshot implements Snapshot<AttributeInstance> {

	private double baseValue;
	
	private List<Map<String, Object>> serializedModifiers;
	
	@Override
	public Snapshot<AttributeInstance> snapshot(AttributeInstance item) {
		deepSnapshot(item);
		return this;
	}

	@Override
	public Snapshot<AttributeInstance> deepSnapshot(AttributeInstance item) {
		baseValue = item.getBaseValue();
		serializedModifiers = new LinkedList<>();
		for (var modifier : item.getModifiers()) {
			serializedModifiers.add(modifier.serialize());
		}
		return this;
	}

	@Override
	public void patch(AttributeInstance item) {
		item.setBaseValue(baseValue);
		// Retrait des modifieurs actuels
		for (var modifier : item.getModifiers()) {
			item.removeModifier(modifier);
		}
		// Ajout des modifiers de la snapshot
		if (serializedModifiers != null) {
			for (var modifier : serializedModifiers) {
				item.addModifier(AttributeModifier.deserialize(modifier));
			}
		}
	}

}
