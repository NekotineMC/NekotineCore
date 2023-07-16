package fr.nekotine.core.text;

public class Text {

	//Pas besoin d'utiliser un StringBuilder, java convertis cette concatenation à la compilation
	
	public static String namedLoggerFormat(String name) {
		return "[" + name + "] >";
	}
	
}
