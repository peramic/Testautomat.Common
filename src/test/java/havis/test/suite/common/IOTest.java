package havis.test.suite.common;

import havis.test.suite.common.helpers.FileHelper;
import havis.test.suite.common.helpers.PathResolverFileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.GenericApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IOTest {

	private GenericApplicationContext objContext;
	private String outputDir;

	@BeforeClass
	public void setUp() throws IOException, URISyntaxException {
		// get objects via spring framework
		objContext = new GenericApplicationContext();
		objContext.refresh();
		outputDir = PathResolverFileHelper.getAbsolutePathFromResource("output")
				.get(0).toString();
		FileHelper.deleteFiles(outputDir);
	}

	@Test
	public void loadResource() throws IOException, URISyntaxException {
		// load an existing file
		IO io = new IO(objContext);
		String content = io
				.loadResource("test/Havis/RfidTestSuite/Testautomat/Common/IOData.txt");
		content = content.substring(content.indexOf('a'));
		Assert.assertEquals(content, "any data: äöüß");

		// try to load an non-existing file
		try {
			content = io.loadResource("test/Havis/RfidTestSuite/Testautomat/Common/a.txt");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("a.txt"));
		}
	}

	@Test
	public void writeResource() throws IOException, URISyntaxException {
		// load an existing file
		IO io = new IO(objContext);
		File file = new File(outputDir, "out.txt");

		io.writeResource(file.getAbsolutePath(), "Some content: äöüß");

		File files = new File(outputDir);
		Assert.assertEquals(files.list().length, 1);

		String content = FileHelper.readFile(file.getAbsolutePath());
		Assert.assertEquals(content, "Some content: äöüß");

		FileHelper.deleteFiles(outputDir);
	}

	@Test
	public void deleteFiles() throws IOException, InterruptedException {

		// create 5 files:
		// 0.xml
		// sub1/1.xml
		// sub1/sub2/2.xml
		// 3.xml
		// x.txt
		File sub1 = new File(outputDir, "sub1");
		sub1.mkdir();
		File sub2 = new File(sub1, "sub2");
		sub2.mkdir();
		String content = "";

		for (int i = 0; i < 1100; i++) {
			content += "a";
		}

		FileHelper.waitForFullSeconds();
		for (int i = 0; i < 4; i++) {
			String dir;
			switch (i) {
			case 1:
				dir = sub1.getAbsolutePath();
				break;
			case 2:
				dir = sub2.getAbsolutePath();
				break;
			default:
				dir = outputDir;
				break;
			}
			// create file (1100 bytes)
			FileHelper.writeFile(new File(dir, i + ".xml").getAbsolutePath(),
					content);
			Thread.sleep(1000);
		}
		FileHelper.writeFile(new File(outputDir, "x.txt").getAbsolutePath(),
				"some content");

		IO io = new IO(objContext);

		// try to delete with non-matching filter
		int count = io.deleteFiles(outputDir, "*.abc", null, null, null, null);
		Assert.assertEquals(count, 0);
		Assert.assertEquals(new File(outputDir).list().length, 4);

		// delete (all) txt file using max. count = 0
		count = io.deleteFiles(outputDir, "*.txt", 0L, null, null, null);
		Assert.assertEquals(count, 1);
		List<File> files = FileHelper.getFiles(outputDir);
		Assert.assertEquals(files.size(), 4);
		for (File file : files) {
			Assert.assertTrue(file.toString().endsWith(".xml"));
		}

		// delete oldest file "0.xml" using max. count = 1
		count = io.deleteFiles(outputDir, "*.xml", 3L, null, null, null);
		Assert.assertEquals(count, 1);
		files = FileHelper.getFiles(outputDir);
		List<String> fileList = new ArrayList<String>();
		Assert.assertEquals(files.size(), 3);
		for (File file : files) {
			fileList.add(file.getName());
		}
		Collections.sort(fileList);
		Assert.assertTrue(fileList.get(0).endsWith("1.xml"));
		Assert.assertTrue(fileList.get(1).endsWith("2.xml"));
		Assert.assertTrue(fileList.get(2).endsWith("3.xml"));

		// delete oldest file "1.xml" using max size = 3 kB ("2.xml" + "3.xml" =
		// 2200 bytes)
		count = io.deleteFiles(outputDir, "*.xml", null, 3L, null, null);
		Assert.assertEquals(count, 1);
		files = FileHelper.getFiles(outputDir);
		fileList = new ArrayList<String>();
		Assert.assertEquals(files.size(), 2);
		for (File file : files) {
			fileList.add(file.getName());
		}
		Collections.sort(fileList);
		Assert.assertTrue(fileList.get(0).endsWith("2.xml"));
		Assert.assertTrue(fileList.get(1).endsWith("3.xml"));

		// delete oldest file "2.xml" using max. time interval = 2 sec
		// the sub directory structure must be deleted
		Thread.sleep(300); // => newest file "3.xml" was written ~ 1.3 sec
							// before
		count = io.deleteFiles(outputDir, "*.xml", null, null, new Date(), 2L);
		Assert.assertEquals(count, 1);
		files = FileHelper.getDirs(outputDir);
		Assert.assertEquals(files.size(), 1);
		files = FileHelper.getFiles(outputDir);
		Assert.assertEquals(files.size(), 1);
		Assert.assertTrue(files.get(0).toString().endsWith("3.xml"));

		FileHelper.deleteFiles(outputDir);
	}

}
