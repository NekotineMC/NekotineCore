package fr.nekotine.core.usable;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.util.ItemStackUtil;
import net.kyori.adventure.text.Component;

public class Usable {
	
	private static final Enchantment GLOW_ENCHANT = Enchantment.DURABILITY;
	
	@NotNull
	private ItemStack item;
	
	public Usable(@NotNull ItemStack item) {
		this.item = item;
	}
	
	public @NotNull ItemStack getItemStack() {
		return item;
	}
	
	public Usable register() {
		Ioc.resolve(UsableModule.class).register(this);
		return this;
	}
	
	public void unregister() {
		Ioc.resolve(UsableModule.class).unregister(this);
	}
	
	protected void OnInteract(PlayerInteractEvent e) {
	}
	protected void OnDrop(PlayerDropItemEvent e) {
	}
	protected void OnInventoryClick(InventoryClickEvent e) {
	}
	protected void OnConsume(PlayerItemConsumeEvent e) {
	}
	protected void OnReadyArrow(PlayerReadyArrowEvent e) {
	}
	protected void OnBowShoot(EntityShootBowEvent e) {
	}
	
	/**
	 * Modifie le nombre d'items dans l'objet
	 * @param quantity
	 */
	public @NotNull Usable setAmount(int quantity) {
		item.setAmount(quantity);
		return this;
	}
	/**
	 * Si l'objet doit afficher le texte de ses enchantements
	 * @param text
	 */
	public @NotNull Usable hideEnchants(boolean hide) {
		if(hide) {
			item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else {
			item.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		return this;
	}
	
	/**
	 * Si l'objet doit briller (SI L'OBJET A DEJA UN ENCHANTEMENT IL EST IMPOSSIBLE DE CACHER CE BRILLEMENT)
	 * @param glow
	 */
	public @NotNull Usable forceEnchantGlint(boolean glow) {
		if(glow) {
			item.addUnsafeEnchantment(GLOW_ENCHANT, 1);
		}else {
			item.removeEnchantment(GLOW_ENCHANT);
		}
		return null;
	}
	/**
	 * Ajoute un enchantement
	 * @param enchantement
	 * @param level
	 */
	public @NotNull Usable addEnchant(Enchantment enchantement, int level) {
		item.addUnsafeEnchantment(enchantement, level);
		return this;
	}
	
	/**
	 * Si l'objet doit être incassable
	 * @param unbreakable
	 */
	public @NotNull Usable setUnbreakable(boolean unbreakable) {
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(unbreakable);
		item.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Si l'objet doit cacher qu'il est incassable
	 * @param hide
	 */
	public @NotNull Usable hideUnbreakable(boolean hide) {
		if(hide) {
			item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}else {
			item.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		return this;
	}
	
	/**
	 * Change le nom de l'objet
	 * @param name
	 */
	public @NotNull Usable setName(String name) {
		setName(Component.text(name));
		return this;
	}
	/**
	 * Change le nom de l'objet
	 * @param name
	 */
	public @NotNull Usable setName(Component name) {
		ItemMeta meta = item.getItemMeta();
		meta.displayName(name);
		item.setItemMeta(meta);
		return this;
	}
	/**
	 * Change la description de l'objet
	 * @param lore
	 */
	public @NotNull Usable setLore(String... lore) {
		setLore(Arrays.stream(lore).map(s -> Component.text(s)).toArray(Component[]::new));
		return this;
	}
	
	/**
	 * Change la description de l'objet
	 * @param lore
	 */
	public @NotNull Usable setLore(Component... lore) {
		setLore(Arrays.asList(lore));
		return this;
	}
	
	/**
	 * Change la description de l'objet
	 * @param lore
	 */
	public @NotNull Usable setLore(List<Component> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.lore(lore);
		item.setItemMeta(meta);
		return this;
	}
	
	/**
	 * Change le matériau de l'objet
	 * @param material
	 */
	public @NotNull Usable setMaterial(Material material) {
		item.setType(material);
		return this;
	}
	
	/**
	 * Ne fonctionne qu'avec les objets ayant une durabilité
	 * @param durability La durabilité restante de l'objet
	 */
	public @NotNull Usable setDurability(int durability) {
		var meta = item.getItemMeta();
		if (meta instanceof Damageable damageable) {
			damageable.setDamage(durability);
			item.setItemMeta(damageable);
		}
		return this;
	}
	
	/**
	 * Ne fonctionne qu'avec les objets ayant une durabilité
	 * @param durability Le pourcentage de durabilité de l'objet
	 */
	public @NotNull Usable setDurabilityPercentage(float percentage) {
		if (percentage < 0 || percentage > 1) {
			throw new IllegalArgumentException("le pourcentage donné (" + percentage + ") doit être compris entre 0 et 1.");
		}
		var meta = item.getItemMeta();
		if (meta instanceof Damageable damageable) {
			damageable.setDamage((int) (item.getType().getMaxDurability() * percentage));
			item.setItemMeta(damageable);
		}
		return this;
	}
	
	public @NotNull Usable setUnstackable() {
		ItemStackUtil.setUnstackable(item);
		return this;
	}
	
	
}
