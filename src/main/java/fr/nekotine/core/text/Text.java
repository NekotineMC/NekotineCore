package fr.nekotine.core.text;

import fr.nekotine.core.module.PluginModule;

public class Text {

	public static String moduleLog(PluginModule module, String text) {
		return new StringBuilder(TextColor.loggingModule)
				.append(module.getName()).append('>')
				.append(TextColor.loggingInfo).append(text).toString();
	}
	
}
