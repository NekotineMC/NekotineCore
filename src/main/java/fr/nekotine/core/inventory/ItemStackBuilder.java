package fr.nekotine.core.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.nekotine.core.tuple.Pair;
import net.kyori.adventure.text.Component;

/**
 * Usine à {@link org.bukkit.inventory.ItemStack ItemStack}. Les méthodes de cette classe ne sont pas pures!
 * @author XxGoldenbluexX
 *
 */
public class ItemStackBuilder {

	private Material material;
	
	private @Nullable Component name;
	
	private int amount;
	
	private List<Component> lore;
	
	private Map<Enchantment,Integer> enchantments;
	
	private List<Pair<Attribute, AttributeModifier>> attributeModifiers;
	
	private Set<ItemFlag> flags;
	
	private boolean unbreakable;
	
	public ItemStackBuilder(Material material) {
		this.material = material;
		lore = new LinkedList<>();
		enchantments = new HashMap<>();
		attributeModifiers = new LinkedList<>();
		flags = new HashSet<>();
	}
	
	public ItemStackBuilder(ItemStackBuilder other) {
		material = other.material;
		name = other.name;
		amount = other.amount;
		lore = new LinkedList<>(other.lore);
		enchantments = new HashMap<>(other.enchantments);
		attributeModifiers = new LinkedList<>(other.attributeModifiers);
		flags = new HashSet<>(other.flags);
		unbreakable = other.unbreakable;
	}
	
	public ItemStackBuilder material(Material material) {
		this.material = material;
		return this;
	}
	
	public ItemStackBuilder name(@Nullable Component title) {
		this.name = title;
		return this;
	}
	
	public ItemStackBuilder amount(int amount) {
		this.amount = amount;
		return this;
	}
	
	public ItemStackBuilder lore(List<Component> lore) {
		this.lore = lore;
		return this;
	}
	
	public ItemStackBuilder appendLore(Component loreLine) {
		this.lore.add(loreLine);
		return this;
	}
	
	public ItemStackBuilder enchant(Enchantment enchant, int level) {
		enchantments.put(enchant, level);
		return this;
	}
	
	public ItemStackBuilder attributeModifier(Attribute attribute, AttributeModifier modifier) {
		attributeModifiers.add(new Pair<>(attribute, modifier));
		return this;
	}
	
	public ItemStackBuilder flags(ItemFlag... flags) {
		this.flags.addAll(Arrays.asList(flags));
		return this;
	}
	
	public ItemStackBuilder unbreakable(boolean unbreakable) {
		this.unbreakable = unbreakable;
		return this;
	}
	
	public ItemStackBuilder unbreakable() {
		this.unbreakable = true;
		return this;
	}
	
	public ItemStackBuilder oldPvpAtkSpd() {
		return attributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("pvp_1.8", 1000.0D, AttributeModifier.Operation.ADD_NUMBER));
	}
	
	/**
	 * Créée une nouvelle instance d'ItemStack
	 * @return
	 */
	public ItemStack make() {
		var itemStack = new ItemStack(material);
		itemStack.setAmount(amount);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		meta.lore(lore);
		for (var ench : enchantments.entrySet()) {
			meta.addEnchant(ench.getKey(), ench.getValue(), true);
		}
		for (var attr : attributeModifiers) {
			meta.addAttributeModifier(attr.a(), attr.b());
		}
		meta.addItemFlags(flags.toArray(ItemFlag[]::new));
		meta.setUnbreakable(unbreakable);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public ItemStack build() {
		return make();
	}
	
}
