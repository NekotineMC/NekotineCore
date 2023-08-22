package fr.nekotine.core.util;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import fr.nekotine.core.NekotineCore;
import net.kyori.adventure.text.Component;

public class ItemStackUtil {

	private static int unstackableCounter = Integer.MIN_VALUE;
	
	private static final NamespacedKey unstackableKey = new NamespacedKey(NekotineCore.getAttachedPlugin(), "Unstackable");
	
	/**
	 * Retire tous les enchantments de l'item.
	 * @param itemStack
	 */
	public static void clearEnchants(ItemStack itemStack) {
		var meta = itemStack.getItemMeta();
		for (var ench : Enchantment.values()) {
			meta.removeEnchant(ench);
		}
		itemStack.setItemMeta(meta);
	}
	
	/**
	 * Applique tous les flags qui cache les propriétés de l'item.
	 */
	public static void hideAllFlags(ItemStack itemStack) {
		itemStack.addItemFlags(ItemFlag.values());
	}
	
	public static void setUnstackable(ItemStack itemStack) {
		var meta = itemStack.getItemMeta();
		var container = meta.getPersistentDataContainer();
		container.set(unstackableKey, PersistentDataType.INTEGER, unstackableCounter++);
		itemStack.setItemMeta(meta);
	}
	
	public static @NotNull ItemStack skull(String url) {
		var itemStack = new ItemStack(Material.PLAYER_HEAD);
		try {
			var meta = itemStack.getItemMeta();
			if (meta instanceof SkullMeta skullMeta) {
				var profile = Bukkit.createProfile(UUID.randomUUID());
				var texture = profile.getTextures();
				texture.setSkin(new URL(url));
				profile.setTextures(texture);
				skullMeta.setPlayerProfile(profile);
				itemStack.setItemMeta(skullMeta);
			}
		}catch(Exception e) {
		}
		return itemStack;
	}
	
	public static @NotNull ItemStack skull(URL url) {
		var itemStack = new ItemStack(Material.PLAYER_HEAD);
		var meta = itemStack.getItemMeta();
		if (meta instanceof SkullMeta skullMeta) {
			var profile = Bukkit.createProfile(UUID.randomUUID());
			var texture = profile.getTextures();
			texture.setSkin(url);
			profile.setTextures(texture);
			skullMeta.setPlayerProfile(profile);
			itemStack.setItemMeta(skullMeta);
		}
		return itemStack;
	}
	
	/// MAKE SHORTCUTS
	
	public static @NotNull ItemStack make(Material material, Component name, List<Component> lore) {
		var itemStack = new ItemStack(material);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		meta.lore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static @NotNull ItemStack make(Material material, Component name, Component ... lore) {
		var itemStack = new ItemStack(material);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		meta.lore(Arrays.asList(lore));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static @NotNull ItemStack make(Material material, int amount, Component name, List<Component> lore) {
		var itemStack = new ItemStack(material, amount);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		meta.lore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static @NotNull ItemStack make(Material material, int amount, Component name, Component ... lore) {
		var itemStack = new ItemStack(material, amount);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		meta.lore(Arrays.asList(lore));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static @NotNull ItemStack make(Material material, Component name) {
		var itemStack = new ItemStack(material);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static @NotNull ItemStack make(Material material, int amount, Component name) {
		var itemStack = new ItemStack(material, amount);
		var meta = itemStack.getItemMeta();
		meta.displayName(name);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
}
