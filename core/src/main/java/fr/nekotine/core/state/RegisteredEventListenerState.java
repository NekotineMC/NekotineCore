package fr.nekotine.core.state;

import fr.nekotine.core.util.EventUtil;
import org.bukkit.event.Listener;

public class RegisteredEventListenerState implements State {

	private Listener listener;

	public RegisteredEventListenerState(Listener listener) {
		this.listener = listener;
	}

	@Override
	public void setup() {
		EventUtil.register(listener);
	}

	@Override
	public void teardown() {
		EventUtil.unregister(listener);
	}
}
