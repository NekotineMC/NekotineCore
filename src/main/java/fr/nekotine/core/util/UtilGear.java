package fr.nekotine.core.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UtilGear {
	
	/**
	 * Compare le Material de l'item & celui donné
	 * @param item L'Itemstack à regarder
	 * @param material Le Material à comparer
	 * @return True si égaux
	 */
	public static boolean IsMaterial(ItemStack item, Material material) {
		return item.getType()==material;
	}
	
	/**
	 * 
	 * @param player Le joueur à controler
	 * @param material Le Material à controler
	 * @return True si le Material est dans l'inventaire du joueur
	 */
	public static boolean HasMaterial(Player player, Material material) {
		return player.getInventory().contains(material);
	}
	
	/**
	 * 
	 * @param player Le joueur à controler
	 * @param material Le Material à controler
	 * @return True si le Material est dans sa main principale
	 */
	public static boolean HasInMainHand(Player player, Material material) {
		return IsMaterial(GetSlot(player, EquipmentSlot.HAND), material);
	}
	
	/**
	 * 
	 * @param player Le joueur à controler
	 * @param material Le Material à controler
	 * @return True si le Material est dans sa main secondaire
	 */
	public static boolean HasInOffHand(Player player, Material material) {
		return IsMaterial(GetSlot(player, EquipmentSlot.OFF_HAND), material);
	}
	
	/**
	 * 
	 * @param player Le joueur à controler
	 * @param material Le Material à controler
	 * @return True si le Material est dans une de ses mains
	 */
	public static boolean HasInAnyHand(Player player, Material material) {
		return HasInMainHand(player, material) || HasInOffHand(player,material);
	}
	
	/**
	 * 
	 * @param player Le joueur à inspecter
	 * @param slot L'EquipmentSlot à inspecter
	 * @return L'ItemStack de l'EquipmentSlot en question
	 */
	public static ItemStack GetSlot(Player player, EquipmentSlot slot) {
		switch(slot) {
		case CHEST:
			return player.getEquipment().getChestplate();
		case FEET:
			return player.getEquipment().getBoots();
		case HAND:
			return player.getEquipment().getItemInMainHand();
		case LEGS:
			return player.getEquipment().getLeggings();
		case OFF_HAND:
			return player.getEquipment().getItemInOffHand();
		default:
			return null;
		}
	}
}
