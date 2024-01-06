package fr.nekotine.core.structure;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;

public class Structure {
	private EditSession session;
	public Structure(EditSession session) {
		this.session = session;
	}
	public void remove() {
		try(var newSession = WorldEdit.getInstance().newEditSession(session.getWorld())){
			session.undo(newSession);
			session = newSession;
		}
	}
}
