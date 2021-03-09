package havis.test.suite.common.messaging;

import havis.test.suite.common.IO;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.jibx.runtime.JiBXException;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLMessage {

	private final String xml;
	private final Object obj;
	private final Class<?> type;

	/**
	 * This constructor takes a XML String and a type to deserialize to. Can be
	 * used to deserialize and normalize.
	 * 
	 * @param xml
	 * @param type
	 */
	public XMLMessage(String xml, Class<?> type) {
		obj = null;
		this.type = type;
		this.xml = xml;
	}

	/**
	 * This constructor takes a XML String. Use only to normalize XML-Data
	 * 
	 * @param xml
	 */
	public XMLMessage(String xml) {
		obj = null;
		type = null;
		this.xml = xml;
	}

	/**
	 * This constructor takes an object to serialize
	 * 
	 * @param obj
	 */
	public XMLMessage(Object obj) {
		xml = null;
		if (obj != null) {
			type = obj.getClass();
		} else {
			type = null;
		}
		this.obj = obj;
	}

	/**
	 * Get a DOM Document from XML-String declared in constructor
	 * 
	 * @return null, if xml string is not declared
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDomDocument() throws ParserConfigurationException,
			SAXException, IOException {

		Document doc = null;
		if (xml != null) {
			// Build DOM-Document
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			// Anonymous class to suppress error messages
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception)
						throws SAXException {
				}

				@Override
				public void fatalError(SAXParseException exception)
						throws SAXException {
				}

				@Override
				public void error(SAXParseException exception)
						throws SAXException {
				}
			});
			doc = builder.parse(new InputSource(new StringReader(xml)));
		}

		return doc;
	}

	/**
	 * Get a normalized XML-String from XML-String declared in constructor
	 * 
	 * @return null, if xml string is not declared
	 * @throws XMLNormalizationException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public String normalize() throws XMLNormalizationException,
			ParserConfigurationException, SAXException, IOException {

		if (xml != null) {
			// Get DOM-Document from XML-String
			Document doc = getDomDocument();
			DOMImplementation domImplementation = doc.getImplementation();
			// Check Implementation Version, if it have pretty print option
			if (domImplementation.hasFeature("LS", "3.0")
					&& domImplementation.hasFeature("Core", "2.0")) {
				DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation
						.getFeature("LS", "3.0");
				// Build Serializer
				LSSerializer lsSerializer = domImplementationLS
						.createLSSerializer();
				DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
				// Pretty print
				if (domConfiguration.canSetParameter("format-pretty-print",
						Boolean.TRUE)) {
					domConfiguration.setParameter("format-pretty-print",
							Boolean.TRUE);
					LSOutput lsOutput = domImplementationLS.createLSOutput();
					lsOutput.setEncoding(StandardCharsets.UTF_8.name());
					StringWriter stringWriter = new StringWriter();
					lsOutput.setCharacterStream(stringWriter);
					// Write pretty
					lsSerializer.write(doc, lsOutput);

					String result = stringWriter.toString();
					// Check line separator
					return IO.normalizeToFileString(result);
				} else {
					throw new XMLNormalizationException(
							"DOMConfiguration 'format-pretty-print' parameter isn't settable.");
				}
			} else {
				throw new XMLNormalizationException(
						"DOM 3.0 LS and/or DOM 2.0 Core not supported.");
			}
		}
		return null;
	}

	/**
	 * Get serialized String from object declared in constructor. Object must be
	 * binded with JIBX
	 * 
	 * @return null, if object is not declared
	 * @throws JiBXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public String getSerializationString(String rootName, String ns) throws IOException, JAXBException {
		return getSerializationString(rootName, ns, null);
	}
	
	/**
	 * Get serialized String from object declared in constructor. Object must be
	 * binded with JIBX
	 * 
	 * @return null, if object is not declared
	 * @throws JiBXException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public String getSerializationString(String rootName, String ns, XMLStreamWriterFactory factory) throws IOException, JAXBException {
		String result = null;
		if (obj != null) {
			JAXBContext jaxbContext = JAXBContext.newInstance(type);
			Marshaller marshaller = jaxbContext.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			QName qName = new QName(ns, rootName);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JAXBElement<?> root = new JAXBElement(qName, type, obj);
			if (factory != null) {
				marshaller.marshal(root, factory.create(stringWriter));
			} else {
				marshaller.marshal(root, stringWriter);
			}
			result = stringWriter.toString();
			stringWriter.close();
			return IO.normalizeToFileString(result);
		}
		return null;
	}

	/**
	 * Get an Object from XML-String declared in constructor. Object must be
	 * binded with JIBX
	 * 
	 * @return null, if xml or type is not declared in constructor
	 * @throws JAXBException
	 */
	public Object getDeserializedObject() throws JAXBException {
		String xml = this.xml.substring(this.xml.indexOf("<"));
		if (xml != null && type != null) {
			JAXBContext jaxbContext = JAXBContext.newInstance(type);
			StringReader reader = new StringReader(xml);
			// Unmarshalling
			return jaxbContext.createUnmarshaller()
					.unmarshal(new StreamSource(reader), type).getValue();
		}
		return null;
	}

}
