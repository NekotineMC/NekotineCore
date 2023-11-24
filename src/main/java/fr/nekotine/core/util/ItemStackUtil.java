package fr.nekotine.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.profile.PlayerProfile;

import fr.nekotine.core.NekotineCore;
import net.kyori.adventure.text.Component;

public class ItemStackUtil {

	private static int unstackableCounter = Integer.MIN_VALUE;
	
	private static final NamespacedKey unstackableKey = new NamespacedKey(NekotineCore.getAttachedPlugin(), "UnstackableId");
	
	/**
	 * Retire tous les enchantments de l'item.
	 * @param itemStack
	 */
	public static ItemStack clearEnchants(ItemStack itemStack) {
		var meta = itemStack.getItemMeta();
		for (var ench : Enchantment.values()) {
			meta.removeEnchant(ench);
		}
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	/**
	 * Ajoute un enchantments à l'item.
	 * @param itemStack
	 * @param enchant
	 * @param level
	 */
	public static ItemStack addEnchant(ItemStack itemStack, Enchantment enchant, int level) {
		itemStack.addUnsafeEnchantment(enchant, level);
		return itemStack;
	}
	
	/**
	 * Applique tous les flags qui cache les propriétés de l'item.
	 */
	public static ItemStack hideAllFlags(ItemStack itemStack) {
		itemStack.addItemFlags(ItemFlag.values());
		return itemStack;
	}
	
	public static ItemStack setUnbreakable(ItemStack itemStack, boolean unbreakable) {
		var meta = itemStack.getItemMeta();
		meta.setUnbreakable(unbreakable);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static ItemStack setOldPvpAttackSpeed(ItemStack itemStack, boolean unbreakable) {
		var meta = itemStack.getItemMeta();
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("pvp_1.8", 1000.0D, AttributeModifier.Operation.ADD_NUMBER));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static ItemStack setFlags(ItemStack itemStack, ItemFlag... flags) {
		var meta = itemStack.getItemMeta();
		meta.removeItemFlags(meta.getItemFlags().toArray(ItemFlag[]::new));
		meta.addItemFlags(flags);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static ItemStack setUnstackable(ItemStack itemStack, boolean unstackable) {
		var meta = itemStack.getItemMeta();
		var container = meta.getPersistentDataContainer();
		if (unstackable) {
			container.set(unstackableKey, PersistentDataType.INTEGER, unstackableCounter++);
		}else {
			container.remove(unstackableKey);
		}
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public static ItemMeta setUnstackable(ItemMeta itemMeta, boolean unstackable) {
		var container = itemMeta.getPersistentDataContainer();
		if (unstackable) {
			container.set(unstackableKey, PersistentDataType.INTEGER, unstackableCounter++);
		}else {
			container.remove(unstackableKey);
		}
		return itemMeta;
	}
	
	public static ItemStack setUnstackable(ItemStack itemStack) {
		setUnstackable(itemStack, true);
		return itemStack;
	}
	
	public static ItemMeta setUnstackable(ItemMeta itemMeta) {
		return setUnstackable(itemMeta, true);
	}
	
	public static @NotNull ItemStack skull(String url) throws MalformedURLException, URISyntaxException {
		return skull(new URI(url).toURL());
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
	
	public static @NotNull ItemStack skull(PlayerProfile profile) {
		var itemStack = new ItemStack(Material.PLAYER_HEAD);
		if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
			skullMeta.setPlayerProfile(profile);
			itemStack.setItemMeta(skullMeta);
		}
		return itemStack;
	}
	
	public static @NotNull ItemStack skull(OfflinePlayer player) {
		var itemStack = new ItemStack(Material.PLAYER_HEAD);
		if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
			skullMeta.setPlayerProfile(player.getPlayerProfile());
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
	
	public static void changeTo(ItemStack target, ItemStack source) {
		target.setType(source.getType());
		target.setAmount(source.getAmount());
		target.setItemMeta(source.getItemMeta());
	}
	
	public static boolean isAirOrNull(ItemStack item){
		return item == null || item.getType().equals(Material.AIR);
	}
}
