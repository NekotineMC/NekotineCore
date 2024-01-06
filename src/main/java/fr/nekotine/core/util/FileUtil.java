package fr.nekotine.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	
	public static void createNewFile(File file, InputStream input) throws FileNotFoundException, IOException {
		var parFile = file.getParentFile();
		if(parFile!=null)
			parFile.mkdirs();
		if(file.createNewFile()) {
			var output = new FileOutputStream(file);
			input.transferTo(output);
			output.close();
		}
	}
	
}
