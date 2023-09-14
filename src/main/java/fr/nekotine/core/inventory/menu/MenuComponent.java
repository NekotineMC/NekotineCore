package fr.nekotine.core.inventory.menu;

public abstract class MenuComponent {

	private MenuComponent parent;
	
	public void setParent(MenuComponent parent) {
		this.parent = parent;
	}
	
	public MenuComponent getParent() {
		return parent;
	}
	
	public void askRedraw() {
		parent.askRedraw();
	}
	
}
