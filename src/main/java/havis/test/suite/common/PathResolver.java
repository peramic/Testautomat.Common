package havis.test.suite.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PathResolver {

	/**
	 * Get absolute paths of classpath resources
	 * 
	 * @param cpResource
	 * @return an empty list, if resource was not found
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<Path> getAbsolutePathFromResource(String path,
			String cpResource) throws IOException, URISyntaxException {
		path = path.replace(File.separator, "/");
		ArrayList<Path> ret = new ArrayList<Path>();
		ClassLoader loader = PathResolver.class.getClassLoader();
		Enumeration<URL> resource = loader.getResources(path);
		while (resource.hasMoreElements()) {
			URL url = resource.nextElement();
			JarURLConnection urlcon = (JarURLConnection) (url.openConnection());
			try (JarFile jar = urlcon.getJarFile();) {
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(path) && name.endsWith(cpResource)) {
						ret.add(Paths.get(name).getParent());
					}
				}
			}
		}
		return ret;
	}

	public static URL getUrlFromResource(String path, String cpResource)
			throws IOException {
		String file = path.replace(File.separator, "/") + "/" + cpResource;
		file = URI.create(file).normalize().getPath();
		ClassLoader loader = PathResolver.class.getClassLoader();
		return loader.getResource(file);
	}

	public static boolean fileExists(String path, String cpResource)
			throws IOException, URISyntaxException {
		String cp = path.replace(File.separator, "/") + "/" + cpResource;
		ClassLoader loader = PathResolver.class.getClassLoader();
		URL resource = loader.getResource(cp);
		if (resource == null) {
			return false;
		}
		return true;
	}

	public static InputStream getResourceInputStream(String path,
			String cpResource) {
		String file = path.replace(File.separator, "/") + "/" + cpResource;
		file = URI.create(file).normalize().getPath();
		ClassLoader loader = PathResolver.class.getClassLoader();
		return loader.getResourceAsStream(file);
	}

	public static InputStream getResourceInputStream(String cpResource) {
		String file = cpResource.replace(File.separator, "/");
		file = URI.create(file).normalize().getPath();
		ClassLoader loader = PathResolver.class.getClassLoader();
		return loader.getResourceAsStream(file);
	}

	/**
	 * Get parent path of an file/dir
	 * 
	 * @param filename
	 *            absolute path
	 * @return
	 */
	public static Path getDirectoryFromFile(String filename) {
		Path ret = null;
		File file = new File(filename);
		if (file.exists())
			ret = Paths.get(file.getParent());
		return ret;
	}

}
