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
import fr.nekotine.core.util.ItemStackUtil;
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
	
	private boolean unstackable;
	
	private boolean oldPvp;
	
	public ItemStackBuilder(Material material) {
		this.material = material;
		amount = 1;
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
		unstackable = other.unstackable;
		oldPvp = other.oldPvp;
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
	
	public ItemStackBuilder enchant(Enchantment enchant) {
		enchantments.put(enchant, 1);
		return this;
	}
	
	/**
	 * Donne un enchantement par défaut pour l'effet visuel.
	 * Le flag pour cacher les enchantments est ajouté automatiquement
	 * <p>
	 * Note: L'enchantement utilisé est {@link org.bukkit.enchantments.Enchantment VANISHING_CURSE}
	 * </p>
	 * @return
	 */
	public ItemStackBuilder enchant() {
		flags.add(ItemFlag.HIDE_ENCHANTS);
		enchantments.put(Enchantment.VANISHING_CURSE, 1);
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
	
	public ItemStackBuilder unstackable(boolean unstackable) {
		this.unstackable = unstackable;
		return this;
	}
	
	public ItemStackBuilder unstackable() {
		this.unstackable = true;
		return this;
	}
	
	public ItemStackBuilder oldPvp() {
		oldPvp = true;
		return this;
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
		applyOldPvp();
		for (var attr : attributeModifiers) {
			meta.addAttributeModifier(attr.a(), attr.b());
		}
		meta.addItemFlags(flags.toArray(ItemFlag[]::new));
		meta.setUnbreakable(unbreakable);
		ItemStackUtil.setUnstackable(meta, unstackable);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public ItemStack build() {
		return make();
	}
	
	private void applyOldPvp() {
		switch(material) {
		case WOODEN_SWORD:
		case WOODEN_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 3.2D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case STONE_SWORD:
		case STONE_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 3.2D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case IRON_SWORD:
		case IRON_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 4D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case GOLDEN_SWORD:
		case GOLDEN_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 4.8D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case DIAMOND_SWORD:
		case DIAMOND_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 5.6D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		case NETHERITE_SWORD:
		case NETHERITE_AXE:
			attributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("pvp_1.8", 6.4D, AttributeModifier.Operation.ADD_NUMBER));
			break;
		default:
			break;
		}
		attributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("pvp_1.8", 100D, AttributeModifier.Operation.ADD_NUMBER));
	}
	
}
