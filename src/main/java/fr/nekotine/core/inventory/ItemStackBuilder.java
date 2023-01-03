package fr.nekotine.core.inventory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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
	
	public ItemStackBuilder(Material material) {
		this.material = material;
		lore = new LinkedList<>();
		enchantments = new HashMap<>();
	}
	
	public ItemStackBuilder(ItemStackBuilder other) {
		material = other.material;
		name = other.name;
		amount = other.amount;
		lore = new LinkedList<>(other.lore);
		enchantments = new HashMap<>(other.enchantments);
	}
	
	public ItemStackBuilder material(Material material) {
		this.material = material;
		return this;
	}
	
	public ItemStackBuilder title(@Nullable Component title) {
		this.name = title;
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
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
}
