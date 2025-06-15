package fr.nekotine.core.inventory.menu.element;

import fr.nekotine.core.inventory.menu.ClickableMenuComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * MenuElement pour afficher un Component dynamique
 * 
 * @author XxGoldenbluexX
 *
 */
public class ClickableDisplayMenuItem extends MenuElement implements ClickableMenuComponent {

	private final Supplier<Component> valueSupplier;

	private ItemStack item;

	private Consumer<Player> click;

	public ClickableDisplayMenuItem(ItemStack item, Supplier<Component> valueSupplier, Consumer<Player> onClick) {
		this.item = item;
		this.valueSupplier= valueSupplier;
		this.click = onClick;
	}

	@Override
	public ItemStack draw() {
		var meta = item.getItemMeta();
		meta.displayName(valueSupplier.get());
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if (click == null){
			return;
		}
		if (!(event.getWhoClicked() instanceof Player p)){
			return;
		}
		var item = event.getCurrentItem();
		if (item != null && item.isSimilar(draw())) {
			click.accept(p);
			askRedraw();
		}
	}
}
