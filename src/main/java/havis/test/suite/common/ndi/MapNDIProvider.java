package havis.test.suite.common.ndi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import havis.test.suite.api.NDIProvider;

/**
 * Implements (see {@link Havis.RfidTestSuite.Interfaces.NDIProvider}) with a
 * simple dictionary as data source.
 */
public class MapNDIProvider implements NDIProvider {

	private final static String delimiter = "/";
	private final Map<String, Object> entries;

	/**
	 * Initializes the instance
	 */
	public MapNDIProvider() {
		entries = new HashMap<String, Object>();
	}

	/**
	 * See see
	 * {@link Havis.RfidTestSuite.Interfaces.NDIProvider#getValue(String, String)}
	 */
	@Override
	public Object getValue(String community, String rootPath) {
		String internalPath = getInternalPath(community, rootPath);
		if (entries.containsKey(internalPath))
			return entries.get(internalPath);
		return null;
	}

	/**
	 * See see
	 * {@link Havis.RfidTestSuite.Interfaces.NDIProvider#getEntries(String, String)}
	 */
	@Override
	public Map<String, Object> getEntries(String community, String path) {
		return getEntries(community, path, false);
	}

	/**
	 * See see
	 * {@link Havis.RfidTestSuite.Interfaces.NDIProvider#getEntries(String, String, boolean)}
	 */
	@Override
	public Map<String, Object> getEntries(String community, String rootPath,
			boolean recursive) {
		Map<String, Object> ret = new HashMap<String, Object>();
		String internalRootPath = getInternalPath(community, rootPath);
		for (Entry<String, Object> entry : entries.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(internalRootPath)) {
				if (key.length() == internalRootPath.length()) {
					ret.put(delimiter, entry.getValue());
				} else if (recursive
						|| key.indexOf(delimiter, internalRootPath.length()) == key
								.length() - 1) {
					String path = key.substring(internalRootPath.length() - 1,
							key.length() - internalRootPath.length());
					ret.put(path, entry.getValue());
				}
			}
		}
		return ret;
	}

	/**
	 * See see
	 * {@link Havis.RfidTestSuite.Interfaces.NDIProvider#setValue(String, String, Object)}
	 */
	@Override
	public void setValue(String community, String path, Object value) {
		String internalPath = getInternalPath(community, path);
		entries.put(internalPath, value);
	}

	/**
	 * See see
	 * {@link Havis.RfidTestSuite.Interfaces.NDIProvider#removeValue(String, String)}
	 */
	@Override
	public void removeValue(String community, String path) {
		String internalPath = getInternalPath(community, path);
		entries.remove(internalPath);
	}

	/**
	 * Returns the internal path. An internal path starts with the community.
	 * The path is appended to the community with a separating "/". The internal
	 * path ends with "/"
	 * 
	 * @param community
	 * @param path
	 * @return
	 */
	private static String getInternalPath(String community, String path) {
		// if path does not start with delimiter
		if (!path.startsWith(delimiter)) {
			// prepend delimiter
			path = delimiter + path;
		}
		// if not path ends with delimiter
		if (!path.endsWith(delimiter)) {
			// add trailing delimiter
			path += delimiter;
		}
		return community + path;
	}
}
