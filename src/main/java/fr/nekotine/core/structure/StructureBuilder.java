package fr.nekotine.core.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;

import fr.nekotine.core.ioc.Ioc;
import fr.nekotine.core.util.FileUtil;

public class StructureBuilder {
	private ClipboardHolder holder;
	private boolean copyBiomes = true;
	private boolean copyEntities = true;
	private boolean ignoreAirBlocks = false;
	public StructureBuilder(String pathInJar, String pathInPluginFolder) {
		File file = new File(Ioc.resolve(JavaPlugin.class).getDataFolder(), pathInPluginFolder);
		if(!file.exists()) {
			try {
				FileUtil.createNewFile(file, Ioc.resolve(JavaPlugin.class).getResource(pathInJar));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
		    holder = new ClipboardHolder(reader.read());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public AffineTransform affine() {
		return new AffineTransform();
	}
	public StructureBuilder copyBiomes(boolean toCopy) {
		copyBiomes = toCopy;
		return this;
	}
	public StructureBuilder copyEntities(boolean toCopy) {
		copyEntities = toCopy;
		return this;
	}
	public StructureBuilder ignoreAirBlocks(boolean toIgnore) {
		ignoreAirBlocks = toIgnore;
		return this;
	}
	public StructureBuilder transform(Transform transform) {
		holder.setTransform(holder.getTransform().combine(transform));
		return this;
	}
	public StructureBuilder rotateY(double theta) {
		return transform(affine().rotateY(theta));
	}
	public StructureBuilder center() {
		for(var clipboard : holder.getClipboards()) {
			var center = clipboard.getMaximumPoint().add(clipboard.getMinimumPoint()).divide(2);
			clipboard.setOrigin(center);
		}
		return this;
	}
	public Structure paste(Location to) {
		try(EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(to.getWorld()))) {
			Operation operation = holder
		            .createPaste(editSession)
		            .to(BukkitAdapter.asBlockVector(to))
		            .copyBiomes(copyBiomes)
		            .copyEntities(copyEntities)
		            .ignoreAirBlocks(ignoreAirBlocks)
		            .build();
		    Operations.complete(operation);
		    editSession.close();
		    return new Structure(editSession);
		}
	}
}
