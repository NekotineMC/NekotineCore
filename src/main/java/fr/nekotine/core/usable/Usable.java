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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;

import net.kyori.adventure.text.Component;

public class Usable {
	private UsableModule _module;
	private ItemStack item;
	private Inventory _inventory;
	
	private final NamespacedKey key;
	
	private static final Enchantment GLOW_ENCHANT = Enchantment.DURABILITY;
	
	//
	
	public Usable(UsableModule module, ItemStack item) {
		this.item = item;
		_module = module;
		key = new NamespacedKey(_module.getPlugin(), "Unstackable");
		SetUnstackable();
	}
	
	/**
	 * Enregistre ce Usable pour qu'il recoive les événements.
	 * Si l'ItemStack utilisé par ce Usable à déja un Usable associé, l'ajout au registre échoue.
	 * @return si oui ou non le Usable à été ajouté au registre.
	 */
	public boolean register() {
		return _module.register(this);
	}
	
	/**
	 * Retire ce Usable du registre s'il y est présent.
	 * @return Si oui ou non l'Usable a été retiré du registre.
	 */
	public boolean unregister() {
		return _module.unregister(this);
	}
	
	/**
	 * Ajout l'item Usable à l'inventaire.
	 * @param inventory l'inventaire auquel doit être fait l'ajout.
	 */
	public void Give(Inventory inventory) {
		if (_inventory == null) {
			inventory.addItem(item);
			_inventory = inventory;
		}
	}
	
	/**
	 * Supprime l'item Usable de l'inventaire ou il est. Cette methode ne fait rien si l'item n'y est plus.
	 * @param inventory L'inventaire auquel supprimer l'item.
	 */
	public void Remove() {
		if (_inventory != null) {
			_inventory.remove(item);
			_inventory = null;
		}
	}
	
	public void Update(ItemStack previous) {
		if (_inventory != null) {
			int slot = _inventory.first(previous);
			if (slot != -1) {
				_inventory.remove(item);
				_inventory.setItem(slot, item);
			}
		}
	}
	
	//
	
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
	/**
	 * Ne fonctionne qu'avec les objets ayant une durabilité
	 * @param durability La durabilité restante de l'objet
	 */
	public void SetDurability(int durability) {
		if(!(item.getItemMeta() instanceof Damageable)) return;
			
		ItemStack previous = item.clone();
		
		Damageable meta = (Damageable)item.getItemMeta();
		meta.setDamage(durability);
		item.setItemMeta(meta);
			
		Update(previous);
	}
	/**
	 * Ne fonctionne qu'avec les objets ayant une durabilité
	 * @param durability Le pourcentage de durabilité de l'objet
	 */
	public void SetDurabilityPercentage(float percentage) {
		if(!(item.getItemMeta() instanceof Damageable)) return;
			
		ItemStack previous = item.clone();
		
		Damageable meta = (Damageable)item.getItemMeta();
		meta.setDamage((int)Math.ceil(item.getType().getMaxDurability() * percentage));
		item.setItemMeta(meta);
			
		Update(previous);
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
	/**
	 * 
	 * @return L'item utilisé comme Usable
	 */
	public ItemStack getItem() {
		return item;
	}
	
	//
	
	private void SetUnstackable() {
		ItemStack previous = item.clone();
		
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, Math.random());
		item.setItemMeta(meta);
		
		Update(previous);
	}
	
	
}
