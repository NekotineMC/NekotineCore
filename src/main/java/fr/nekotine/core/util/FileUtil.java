package fr.nekotine.core.util;

import java.io.File;

public class FileUtil {

	public static String fileNameWithoutExtension(File file) {
		var name = file.getName();
		if (name.contains(".")) {
			return name.substring(0,name.lastIndexOf('.'));
		}
		return name;
	}
	
	public static String getExtension(File file) {
		var name = file.getName();
		if (name.contains(".")) {
			return name.substring(name.lastIndexOf('.'));
		}
		return "";
	}
	
}
