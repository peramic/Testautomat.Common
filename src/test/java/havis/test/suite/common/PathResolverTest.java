package havis.test.suite.common;

import havis.test.suite.common.helpers.PathResolverFileHelper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

//TODO Test überarbeiten
public class PathResolverTest {

	@Test
	public void getAbsolutePathFromResource() throws IOException,
			URISyntaxException {

		List<Path> paths = new ArrayList<Path>();

		// Unknown resource
		paths = PathResolver.getAbsolutePathFromResource("abcxyz", "");
		Assert.assertEquals(paths.size(), 0);

		// known resource folder
		paths = PathResolverFileHelper.getAbsolutePathFromResource("test");
		Assert.assertTrue(paths.size() >= 1);

		// known resource file
		paths = PathResolverFileHelper
				.getAbsolutePathFromResource("test/Havis/RfidTestSuite/Testautomat/Common/IOData.txt");
		Assert.assertEquals(paths.size(), 1);
	}

	@Test
	public void getDirectoryFromFile() throws IOException, URISyntaxException {

		List<Path> paths = PathResolverFileHelper
				.getAbsolutePathFromResource("test/Havis/RfidTestSuite/Testautomat/Common/IOData.txt");
		Assert.assertEquals(paths.size(), 1);

		// get directory of unknown file
		Path filePath = PathResolver.getDirectoryFromFile(paths.get(0)
				.toString().replace("IOData", "abcxyz"));
		Assert.assertNull(filePath);
		// get directory of known file
		filePath = PathResolver.getDirectoryFromFile(paths.get(0).toString());
		Assert.assertEquals(filePath, paths.get(0).getParent());

	}
}
