package fr.nekotine.core.text;

import fr.nekotine.core.module.PluginModule;

public class Text {

	//Pas besoin d'utiliser un StringBuilder, java convertis cette concatenation à la compilation
	
	public static String moduleLog(PluginModule module, String text) {
		return module.getName()+"> "+text;
	}
	
}
