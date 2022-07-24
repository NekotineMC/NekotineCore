package fr.nekotine.core.usable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import fr.nekotine.core.util.UtilGear;
import net.kyori.adventure.text.Component;

public class Usable {
	private final UsableModule usableManager;
	private ItemStack item;
	private final Inventory inventory;
	
	private Consumer<PlayerInteractEvent> OnInteract;
	private Consumer<PlayerDropItemEvent> OnDrop;
	private Consumer<InventoryClickEvent> OnInventoryClick;
	private Consumer<PlayerItemConsumeEvent> OnConsume;
	private Consumer<PlayerReadyArrowEvent> OnReadyArrow;
	private Consumer<EntityShootBowEvent> OnBowShoot;
	
	private final NamespacedKey key;
	
	private static final Enchantment GLOW_ENCHANT = Enchantment.DURABILITY;
	
	//
	
	public Usable(UsableModule usableManager, ItemStack item, Inventory inventory) {
		this.usableManager = usableManager;
		this.item = item;
		this.inventory = inventory;
		
		key = new NamespacedKey(usableManager.getPlugin(), "Unstackable");
		SetUnstackable();
	}
	
	//
	
	public void OnInteract(PlayerInteractEvent e) {
		if(OnInteract==null) return;
		if(item.equals(e.getItem())) OnInteract.accept(e);
	}
	public void OnDrop(PlayerDropItemEvent e) {
		if(OnDrop==null) return;
		if(item.equals(e.getItemDrop().getItemStack())) OnDrop.accept(e);
	}
	public void OnInventoryClick(InventoryClickEvent e) {
		if(OnInventoryClick==null) return;
		if(item.equals(e.getCurrentItem())) OnInventoryClick.accept(e);
	}
	public void OnConsume(PlayerItemConsumeEvent e) {
		if(OnConsume==null) return;
		if(item.equals(e.getItem())) OnConsume.accept(e);
	}
	public void OnReadyArrow(PlayerReadyArrowEvent e) {
		if(OnReadyArrow==null) return;
		if(item.equals(e.getArrow())) OnReadyArrow.accept(e);
	}
	public void OnBowShoot(EntityShootBowEvent e) {
		if(OnBowShoot==null) return;
		if(item.equals(e.getBow())) OnBowShoot.accept(e);
	}
	
	//
	
	public void AddAmount(int quantity) {
		ItemStack previous = item.clone();
		
		item.add(quantity);
		
		Update(previous);
	}
	public void SetAmount(int quantity) {
		ItemStack previous = item.clone();
		
		item.setAmount(quantity);
		
		Update(previous);
	}
	public void SetEnchantedText(boolean text) {
		ItemStack previous = item.clone();
		
		if(text) {
			item.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else {
			item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		Update(previous);
	}
	public void SetEnchantedGlow(boolean glow) {
		ItemStack previous = item.clone();
		
		if(glow) {
			item.addUnsafeEnchantment(GLOW_ENCHANT, 1);
		}else {
			item.removeEnchantment(GLOW_ENCHANT);
		}
		
		Update(previous);
	}
	public void AddEnchant(Enchantment enchantement, int level) {
		ItemStack previous = item.clone();
		
		item.addUnsafeEnchantment(enchantement, level);
		
		Update(previous);
	}
	public void Remove() {
		UtilGear.Remove(inventory, item);
		
		usableManager.Remove(item);
	}
	public void Give() {
		inventory.addItem(item);
	}
	
	public void SetUnbreakable(boolean unbreakable) {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(unbreakable);
		item.setItemMeta(meta);
		
		Update(previous);
	}
	public void HideUnbreakable(boolean hide) {
		ItemStack previous = item.clone();
		
		if(hide) {
			item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}else {
			item.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		
		Update(previous);
	}
	public void SetName(String name) {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(name));
		item.setItemMeta(meta);
		
		Update(previous);
	}
	public void SetLore(String... lore) {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		List<Component> loreList = new ArrayList<>(); 
		for(String line : lore) {
			if(line!="") {
				loreList.add(Component.text(line));
			}
		}
		meta.lore(loreList);
		item.setItemMeta(meta);
		
		Update(previous);
	}
	public void SetMaterial(Material material) {
		ItemStack previous = item.clone();
		
		ItemStack newItem = new ItemStack(material, item.getAmount());
		newItem.setItemMeta(item.getItemMeta());
		item = newItem;
		
		Update(previous);
	}
	public void OnInteract(Consumer<PlayerInteractEvent> OnInteract) {
		this.OnInteract = OnInteract;
	}
	public void OnDrop(Consumer<PlayerDropItemEvent> OnDrop) {
		this.OnDrop = OnDrop;
	}
	public void OnInventoryClick(Consumer<InventoryClickEvent> OnInventoryClick) {
		this.OnInventoryClick = OnInventoryClick;
	}
	public void OnConsume(Consumer<PlayerItemConsumeEvent> OnConsume) {
		this.OnConsume = OnConsume;
	}
	public void OnReadyArrow(Consumer<PlayerReadyArrowEvent> OnReadyArrow) {
		this.OnReadyArrow = OnReadyArrow;
	}
	public void OnBowShoot(Consumer<EntityShootBowEvent> OnBowShoot) {
		this.OnBowShoot = OnBowShoot;
	}
	
	//
	
	public int GetAmount() {
		return item.getAmount();
	}
	public Material GetMaterial() {
		return item.getType();
	}
	
	//
	
	private void SetUnstackable() {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, Math.random());
		item.setItemMeta(meta);
		
		Update(previous);
	}
	private void Update(ItemStack previous) {
		UtilGear.Replace(inventory, previous, item);
	}
}
