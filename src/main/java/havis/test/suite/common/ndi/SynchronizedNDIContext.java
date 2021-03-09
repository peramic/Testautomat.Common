package havis.test.suite.common.ndi;

import havis.test.suite.api.NDIContext;
import havis.test.suite.api.NDIProvider;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedNDIContext implements NDIContext {

	private NDIProvider provider;

	@Override
	public synchronized void setProvider(NDIProvider provider) {
		this.provider = provider;
	}

	@Override
	public synchronized Object getValue(String community, String path) {
		if (provider != null) {
			return provider.getValue(community, path);
		}
		return null;
	}

	@Override
	public synchronized Map<String, Object> getEntries(String community,
			String path) {
		return getEntries(community, path, false);
	}

	@Override
	public synchronized Map<String, Object> getEntries(String community,
			String path, boolean recursive) {
		if (provider != null)
			return provider.getEntries(community, path, recursive);
		return new HashMap<String, Object>();
	}

	@Override
	public synchronized void setValue(String community, String path,
			Object value) {
		if (provider != null) {
			provider.setValue(community, path, value);
		}
	}

	@Override
	public synchronized void removeValue(String community, String path) {
		if (provider != null) {
			provider.removeValue(community, path);
		}
	}
}
