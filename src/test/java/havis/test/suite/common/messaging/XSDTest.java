package havis.test.suite.common.messaging;

import havis.test.suite.common.XmlValidationException;
import havis.test.suite.common.helpers.FileHelper;
import havis.test.suite.common.helpers.PathResolverFileHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class XSDTest {

	private Path xsdHome = null;

	public XSDTest() throws IOException, URISyntaxException {
		xsdHome = PathResolverFileHelper.getAbsolutePathFromResource(
				"test/Havis/RfidTestSuite/Testautomat/Common/Messaging/XSD")
				.get(0);
		FileHelper.deleteFiles(xsdHome.toString());
	}

	@Test
	public void validate() throws XMLStreamException,
			FactoryConfigurationError, SAXException, IOException,
			ParserConfigurationException {
		// valid XML
		String xsd = String
				.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>%1$s"
						+ "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">%1$s"
						+ "  <xs:element name=\"address\">%1$s"
						+ "    <xs:complexType>%1$s"
						+ "      <xs:sequence>%1$s"
						+ "        <xs:element name=\"name\" type=\"xs:string\"/>%1$s"
						+ "      </xs:sequence>%1$s"
						+ "    </xs:complexType>%1$s" + "  </xs:element>%1$s"
						+ "</xs:schema>", System.getProperty("line.separator"));

		String sampleRequest = String.format(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>%1$s"
						+ "<address>%1$s" + "  <name>John Smith</name>%1$s"
						+ "</address>%1$s",
				System.getProperty("line.separator"));

		String path = xsdHome.toString() + "/test.xsd";
		FileHelper.writeFile(path, xsd);

		InputStream isXsd = PathResolverFileHelper.getResourceInputStream(
				xsdHome.toString(), "test.xsd");
		
		// simple request
		XmlValidationException exception = new XSD(isXsd, "test.xsd")
				.validate(sampleRequest);
		Assert.assertNull(exception);
		
		// No XSD
		exception = new XSD((InputStream)null, "test.xsd").validate(sampleRequest.replace(
				"<name>", "<firstName>"));
		Assert.assertTrue(exception.getMessage().contains("schema"));

		// invalid XML
		isXsd = PathResolverFileHelper.getResourceInputStream(
				xsdHome.toString(), "test.xsd");
		exception = new XSD(isXsd, "test.xsd").validate(sampleRequest.replace(
				"<name>", "<firstName>"));
		Assert.assertTrue(ExceptionSerializer
				.getExceptionStackString(exception).contains("firstName"));
		
		FileHelper.deleteFiles(xsdHome.toString());
	}

}
