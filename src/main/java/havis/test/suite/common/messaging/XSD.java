package havis.test.suite.common.messaging;

import havis.test.suite.common.XmlValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XSD {

	private final Schema schema;
	private final String xsdName;

	/**
	 * Initialize the instance. Loads a XSD-File to build a schema
	 * 
	 * @param xsdFile
	 * @throws SAXException
	 */
	public XSD(InputStream xsdFile, String name) throws SAXException {
		if (xsdFile != null) {
			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = factory.newSchema(new StreamSource(xsdFile));
			xsdName = name;
		} else {
			schema = null;
			xsdName = null;
		}
	}

	public XSD(List<InputStream> xsdFiles, String name) throws SAXException {
		xsdName = name;
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		StreamSource[] streamSources = new StreamSource[xsdFiles.size()];
		for (int i =0; i<xsdFiles.size(); i++){
			streamSources[i] = new StreamSource(xsdFiles.get(i));
		}
		schema = factory.newSchema(streamSources);	
	}

	/**
	 * Validates an XML String against the schema declared in constructor
	 * 
	 * @param xml
	 * @return null, if validation was successfully
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public XmlValidationException validate(String xml)
			throws XMLStreamException, FactoryConfigurationError, SAXException,
			IOException, ParserConfigurationException {
		if (schema != null) {
			StringReader reader = new StringReader(xml);
			javax.xml.validation.Validator validator = schema.newValidator();
			SAXSource sax = new SAXSource(new InputSource(reader));
			try {
				validator.validate(sax);
			} catch (SAXException e) {
				return new XmlValidationException("Validation of " + xsdName, e);
			} finally {
				if (reader != null) {
					reader.close();
				}

			}

		} else {
			return new XmlValidationException("No schema given");
		}
		return null;
	}

}
