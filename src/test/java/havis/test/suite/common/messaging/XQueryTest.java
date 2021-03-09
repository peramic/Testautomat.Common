package havis.test.suite.common.messaging;

import havis.test.suite.common.IO;
import havis.test.suite.common.messaging.XQuery;

import java.io.IOException;
import java.net.URI;

import net.sf.saxon.s9api.SaxonApiException;

import org.testng.Assert;
import org.testng.annotations.Test;


public class XQueryTest {

	private final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final URI uri = createURI();

	private static URI createURI() {
		try {
			return new URI("http://www.HARTING.com");
		} catch (Exception e) {
			return null;
		}
	}

	@Test
	public void execute() throws InterruptedException, SaxonApiException,
			IOException {

		// empty XML
		XQuery xq = new XQuery("root()");
		String result = null;
		try {
			result = xq.execute("", uri);
			Assert.fail();
		} catch (SaxonApiException e) {
			Assert.assertTrue(e.toString().contains("lineNumber"));
		} catch (Exception e) {
			Assert.fail();
		}

		// only XML Definition
		result = xq.execute(xmlHeader, uri);
		Assert.assertEquals(result, xmlHeader);

		// XML without definition
		result = xq.execute("<a>a</a>", uri);
		Assert.assertEquals(result, "<a>a</a>");

		// XML without definition
		result = xq.execute(xmlHeader + "<a>a</a>", uri);
		Assert.assertEquals(result, xmlHeader + "<a>a</a>");

		// get part of XML
		xq = new XQuery("a/b");
		result = IO.normalizeToFileString(xq.execute(
				xmlHeader + "" + System.getProperty("line.separator") + "<a>"
						+ System.getProperty("line.separator") + "  <b>"
						+ System.getProperty("line.separator") + "  <c>c</c>"
						+ System.getProperty("line.separator") + "</b>"
						+ System.getProperty("line.separator") + "</a>", uri));
		Assert.assertEquals(result,
				xmlHeader + "<b>" + System.getProperty("line.separator")
						+ "  <c>c</c>" + System.getProperty("line.separator")
						+ "</b>");

		// transform XML
		xq = new XQuery("<x>{data(a/b)}</x>");
		result = xq.execute(xmlHeader + "<a><b>b</b></a>", uri);
		Assert.assertEquals(result, xmlHeader + "<x>b</x>");

	}

}
