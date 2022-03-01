package humTempC;

import java.io.InputStream;
//import java.io.OutputStream;

/**
 * This static class reads in a resource from the package repository.
 * 
 * @author Erik Hedlund
 *  
 */

final public class ResourceLoader {
	public static InputStream load(String path) {
		InputStream input = ResourceLoader.class.getResourceAsStream(path);
		if (input == null) {
			input = ResourceLoader.class.getResourceAsStream("/"+path);
		}
		return input;
	}
}
