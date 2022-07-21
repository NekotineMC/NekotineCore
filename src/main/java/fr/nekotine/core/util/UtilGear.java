package fr.nekotine.core.util;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class UtilGear {
	
	/**
	 * Compare le Material de l'item & celui donn�
	 * @param item L'Itemstack � regarder
	 * @param material Le Material � comparer
	 * @return True si �gaux
	 */
	public static boolean IsMaterial(ItemStack item, Material material) {
		if(item==null) return false;
		return item.getType()==material;
	}
	
	/**
	 * 
	 * @param entity Le joueur � controler
	 * @param material Le Material � controler
	 * @return True si le Material est dans l'inventaire du joueur
	 */
	public static boolean HasMaterial(InventoryHolder holder, Material material) {
		return holder.getInventory().contains(material);
	}
	
	/**
	 * 
	 * @param entity Le joueur � controler
	 * @param material Le Material � controler
	 * @return True si le Material est dans sa main principale
	 */
	public static boolean HasInMainHand(LivingEntity entity, Material material) {
		return IsMaterial(GetSlot(entity, EquipmentSlot.HAND), material);
	}
	
	/**
	 * 
	 * @param entity Le joueur � controler
	 * @param material Le Material � controler
	 * @return True si le Material est dans sa main secondaire
	 */
	public static boolean HasInOffHand(LivingEntity entity, Material material) {
		return IsMaterial(GetSlot(entity, EquipmentSlot.OFF_HAND), material);
	}
	
	/**
	 * 
	 * @param entity Le joueur � controler
	 * @param material Le Material � controler
	 * @return True si le Material est dans une de ses mains
	 */
	public static boolean HasInAnyHand(LivingEntity entity, Material material) {
		return HasInMainHand(entity, material) || HasInOffHand(entity, material);
	}
	
	/**
	 * 
	 * @param entity Le joueur à controler
	 * @param item L'Item � controler
	 * @return True si l'Item est dans sa main principale
	 */
	public static boolean HasInMainHand(LivingEntity entity, @NotNull ItemStack item) {
		return item.equals(GetSlot(entity, EquipmentSlot.HAND));
	}
	
	/**
	 * 
	 * @param entity Le joueur à controler
	 * @param item Le Material � controler
	 * @return True si l'Item est dans sa main secondaire
	 */
	public static boolean HasInOffHand(LivingEntity entity, @NotNull ItemStack item) {
		return item.equals(GetSlot(entity, EquipmentSlot.OFF_HAND));
	}
	/**
	 * 
	 * @param entity Le joueur � controler
	 * @param item L'Item � controler
	 * @return True si l'Item est dans une de ses mains
	 */
	public static boolean HasInAnyHand(LivingEntity entity, ItemStack item) {
		return HasInMainHand(entity, item) || HasInOffHand(entity, item);
	}
	
	/**
	 * 
	 * @param entity Le joueur � inspecter
	 * @param slot L'EquipmentSlot � inspecter
	 * @return L'ItemStack de l'EquipmentSlot en question
	 */
	public static ItemStack GetSlot(LivingEntity entity, EquipmentSlot slot) {
		switch(slot) {
		case CHEST:
			return entity.getEquipment().getChestplate();
		case FEET:
			return entity.getEquipment().getBoots();
		case HAND:
			return entity.getEquipment().getItemInMainHand();
		case LEGS:
			return entity.getEquipment().getLeggings();
		case OFF_HAND:
			return entity.getEquipment().getItemInOffHand();
		default:
			return null;
		}
	}
	
	/**
	 * 
	 * @param item1
	 * @param item2
	 * @return True si ce sont les m�mes
	 */
	public static boolean IsItem(ItemStack item1, ItemStack item2) {
		return item1.equals(item2);
	}
	
	public static void Replace(Inventory inventory, ItemStack from, ItemStack to) {
		for(Integer slot : inventory.all(from).keySet()) inventory.setItem(slot, to);
		if(inventory instanceof EntityEquipment) {
			EntityEquipment equipmentInventory = (EntityEquipment)inventory;
			
			if(from.equals( equipmentInventory.getHelmet()) ) equipmentInventory.setHelmet(to);
			if(from.equals( equipmentInventory.getChestplate()) ) equipmentInventory.setChestplate(to);
			if(from.equals( equipmentInventory.getLeggings()) ) equipmentInventory.setLeggings(to);
			if(from.equals( equipmentInventory.getBoots()) ) equipmentInventory.setBoots(to);
			if(from.equals( equipmentInventory.getItemInOffHand()) ) equipmentInventory.setItemInOffHand(to);
		}
		if(inventory instanceof PlayerInventory) {
			PlayerInventory playerInventory = (PlayerInventory)inventory;
			
			if(from.equals( playerInventory.getHelmet()) ) playerInventory.setHelmet(to);
			if(from.equals( playerInventory.getChestplate()) ) playerInventory.setChestplate(to);
			if(from.equals( playerInventory.getLeggings()) ) playerInventory.setLeggings(to);
			if(from.equals( playerInventory.getBoots()) ) playerInventory.setBoots(to);
			if(from.equals( playerInventory.getItemInOffHand()) ) playerInventory.setItemInOffHand(to);
		}
	}
	
	public static void Remove(Inventory inventory, ItemStack item) {
		Replace(inventory, item, new ItemStack(Material.AIR));
	}
}
