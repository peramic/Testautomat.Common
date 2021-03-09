package havis.test.suite.common.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PathResolverFileHelper {
	
	/**
	 * Get absolute paths of classpath resources
	 * @param cpRessource
	 * @return an empty list, if resource was not found
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<Path> getAbsolutePathFromResource(String cpRessource)
			throws IOException, URISyntaxException {
		ArrayList<Path> ret = new ArrayList<Path>();
		ClassLoader loader = PathResolverFileHelper.class.getClassLoader();
		System.out.println(cpRessource);
		Enumeration<URL> resource = loader.getResources(cpRessource);
		while (resource.hasMoreElements()) {
			URL elem = resource.nextElement();
			ret.add(Paths.get(new URI(elem.toString())));
		}
		for (Path path : ret) {
			if (!Files.isDirectory(path)) {
				File file = new File(path.toString());
				path = Paths.get(file.getParent());
			}
		}
		return ret;
	}
	
	/**
	 * Get parent path of an file/dir
	 * @param filename absolute path
	 * @return
	 */
	public static Path getDirectoryFromFile(String filename) {
		Path ret = null;
		File file = new File(filename);
		if (file.exists())
			ret = Paths.get(file.getParent());
		return ret;
	}
	
	public static InputStream getResourceInputStream(String path, String cpResource) throws FileNotFoundException{
		File file = new File(path, cpResource);
		return new FileInputStream(file);
	}
	
}
