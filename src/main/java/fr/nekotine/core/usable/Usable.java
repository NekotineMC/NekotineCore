package fr.nekotine.core.usable;

import java.util.ArrayList;
import java.util.List;

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

	private Class<?>[] classes;
	private Function2<PlayerInteractEvent, Class<?>[]> OnInteract;
	private Function2<PlayerDropItemEvent, Class<?>[]> OnDrop;
	private Function2<InventoryClickEvent, Class<?>[]> OnInventoryClick;
	private Function2<PlayerItemConsumeEvent, Class<?>[]> OnConsume;
	private Function2<PlayerReadyArrowEvent, Class<?>[]> OnReadyArrow;
	private Function2<EntityShootBowEvent, Class<?>[]> OnBowShoot;
	
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
	
	protected void OnInteract(PlayerInteractEvent e) {
		if(OnInteract==null) return;
		if(item.equals(e.getItem())) OnInteract.run(e, classes);
	}
	protected void OnDrop(PlayerDropItemEvent e) {
		if(OnDrop==null) return;
		if(item.equals(e.getItemDrop().getItemStack())) OnDrop.run(e, classes);
	}
	protected void OnInventoryClick(InventoryClickEvent e) {
		if(OnInventoryClick==null) return;
		if(item.equals(e.getCurrentItem())) OnInventoryClick.run(e, classes);
	}
	protected void OnConsume(PlayerItemConsumeEvent e) {
		if(OnConsume==null) return;
		if(item.equals(e.getItem())) OnConsume.run(e, classes);
	}
	protected void OnReadyArrow(PlayerReadyArrowEvent e) {
		if(OnReadyArrow==null) return;
		if(item.equals(e.getArrow())) OnReadyArrow.run(e, classes);
	}
	protected void OnBowShoot(EntityShootBowEvent e) {
		if(OnBowShoot==null) return;
		if(item.equals(e.getBow())) OnBowShoot.run(e, classes);
	}
	
	//
	
	/**
	 * Ajoute un nombre d'items dans l'objet 
	 * @param quantity
	 */
	public void AddAmount(int quantity) {
		ItemStack previous = item.clone();
		
		item.add(quantity);
		
		Update(previous);
	}
	/**
	 * Modifie le nombre d'items dans l'objet
	 * @param quantity
	 */
	public void SetAmount(int quantity) {
		ItemStack previous = item.clone();
		
		item.setAmount(quantity);
		
		Update(previous);
	}
	/**
	 * Si l'objet doit afficher le texte de ses enchantements
	 * @param text
	 */
	public void SetEnchantedText(boolean text) {
		ItemStack previous = item.clone();
		
		if(text) {
			item.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else {
			item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		Update(previous);
	}
	/**
	 * Si l'objet doit briller (SI L'OBJET A DEJA UN ENCHANTEMENT IL EST IMPOSSIBLE DE CACHER CE BRILLEMENT)
	 * @param glow
	 */
	public void SetEnchantedGlow(boolean glow) {
		ItemStack previous = item.clone();
		
		if(glow) {
			item.addUnsafeEnchantment(GLOW_ENCHANT, 1);
		}else {
			item.removeEnchantment(GLOW_ENCHANT);
		}
		
		Update(previous);
	}
	/**
	 * Ajoute un enchantement
	 * @param enchantement
	 * @param level
	 */
	public void AddEnchant(Enchantment enchantement, int level) {
		ItemStack previous = item.clone();
		
		item.addUnsafeEnchantment(enchantement, level);
		
		Update(previous);
	}
	/**
	 * Supprime l'objet de l'inventaire & en tant qu'Usable
	 */
	public void Remove() {
		UtilGear.Remove(inventory, item);
		
		usableManager.Remove(item);
	}
	/**
	 * Place l'objet dans l'inventaire
	 */
	public void Give() {
		inventory.addItem(item);
	}
	/**
	 * Si l'objet doit être incassable
	 * @param unbreakable
	 */
	public void SetUnbreakable(boolean unbreakable) {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.setUnbreakable(unbreakable);
		item.setItemMeta(meta);
		
		Update(previous);
	}
	/**
	 * Si l'objet doit cacher qu'il est incassable
	 * @param hide
	 */
	public void HideUnbreakable(boolean hide) {
		ItemStack previous = item.clone();
		
		if(hide) {
			item.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}else {
			item.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		
		Update(previous);
	}
	/**
	 * Change le nom de l'objet
	 * @param name
	 */
	public void SetName(String name) {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(name));
		item.setItemMeta(meta);
		
		Update(previous);
	}
	/**
	 * Change la description de l'objet
	 * @param lore
	 */
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
	/**
	 * Change le matériau de l'objet
	 * @param material
	 */
	public void SetMaterial(Material material) {
		ItemStack previous = item.clone();
		
		ItemStack newItem = new ItemStack(material, item.getAmount());
		newItem.setItemMeta(item.getItemMeta());
		item = newItem;
		
		Update(previous);
	}
	public void SetClasses(Class<?>[] classes) {
		this.classes = classes;
	}
	public void OnInteract(Function2<PlayerInteractEvent, Class<?>[]> OnInteract) {
		this.OnInteract = OnInteract;
	}
	public void OnDrop(Function2<PlayerDropItemEvent, Class<?>[]> OnDrop) {
		this.OnDrop = OnDrop;
	}
	public void OnInventoryClick(Function2<InventoryClickEvent, Class<?>[]> OnInventoryClick) {
		this.OnInventoryClick = OnInventoryClick;
	}
	public void OnConsume(Function2<PlayerItemConsumeEvent, Class<?>[]> OnConsume) {
		this.OnConsume = OnConsume;
	}
	public void OnReadyArrow(Function2<PlayerReadyArrowEvent, Class<?>[]> OnReadyArrow) {
		this.OnReadyArrow = OnReadyArrow;
	}
	public void OnBowShoot(Function2<EntityShootBowEvent, Class<?>[]> OnBowShoot) {
		this.OnBowShoot = OnBowShoot;
	}
	
	//
	
	/**
	 * 
	 * @return Le nombre d'items dans l'objet
	 */
	public int GetAmount() {
		return item.getAmount();
	}
	/**
	 * 
	 * @return Le matériau de l'objet
	 */
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
