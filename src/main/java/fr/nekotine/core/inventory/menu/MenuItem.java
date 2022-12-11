package fr.nekotine.core.inventory.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MenuItem {

	private ItemStack itemStack;
	
	private @Nullable Runnable action;
	
	public MenuItem(ItemStack itemStack, @Nullable Runnable action) {
		this.itemStack = itemStack;
		this.action = action;
	}
	
	public MenuItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public void setItemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
	}
	
	public void setAction(Runnable action) {
		this.action = action;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public Runnable getAction() {
		return action;
	}
	
}
