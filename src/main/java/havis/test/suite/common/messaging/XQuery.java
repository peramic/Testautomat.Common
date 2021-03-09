package havis.test.suite.common.messaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;

public class XQuery {

	private final String xquery;
	private final DocumentBuilder documentBuilder;
	private final XQueryEvaluator evaluator;

	public XQuery(String xquery) throws InterruptedException, SaxonApiException {
		this(xquery, null);
	}

	/**
	 * Initializes the xquery object by compiling the xquery
	 * 
	 * @param xquery
	 * @param baseURI
	 *            identifies the base URI of the xquery document It is used for
	 *            resolving relative URIs contained within the document
	 * @throws InterruptedException
	 * @throws SaxonApiException
	 */
	public XQuery(String xquery, URI baseURI) throws InterruptedException,
			SaxonApiException {
		this.xquery = xquery;

		// Build evaluator
		Processor processor = new Processor(false);
		documentBuilder = processor.newDocumentBuilder();
		XQueryCompiler compiler = processor.newXQueryCompiler();
		XQueryExecutable exp = compiler.compile(this.xquery);
		compiler.setBaseURI(baseURI);
		evaluator = exp.load();
	}

	/**
	 * Executes the query and returns the XML result using UTF-8 encoding
	 * 
	 * @param xml
	 * @param baseURI
	 *            identifies the base URI of the XML document It is used for
	 *            resolving relative URIs contained within the document
	 * @return
	 * @throws SaxonApiException
	 * @throws IOException
	 */
	public String execute(String xml, URI baseURI) throws SaxonApiException,
			IOException {
		if (xml == null) {
			return xml;
		}
		// analyse and prepare XML document
		String trimmedXML = xml.trim();
		boolean xmlDeclExists = trimmedXML.startsWith("<?");
		// if XML declaration exists
		if (xmlDeclExists) {
			// if no body tag
			int i = xml.indexOf("<?");
			i = xml.indexOf('<', i + 1);
			if (i < 0) {
				// nothing to do
				return xml;
			}
		}

		// Build buffers
		BufferedReader inBuffer = new BufferedReader(new StringReader(
				trimmedXML));
		StreamSource in = new StreamSource(inBuffer);
		documentBuilder.setBaseURI(baseURI);
		// compile XML document
		evaluator.setContextItem(documentBuilder.build(in));
		// prepare xquery serializer
		@SuppressWarnings("deprecation")
		Serializer qout = new Serializer();

		qout.setOutputProperty(Serializer.Property.METHOD, "xml");
		qout.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,
				xmlDeclExists ? "no" : "yes");

		// Build output stream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(baos,
				StandardCharsets.UTF_8.name()));

		try {
			// redirect serialisation output to output stream
			qout.setOutputWriter(out);
			// execute xquery for XML document
			evaluator.run(qout);
			// read output stream
			String result = baos.toString(StandardCharsets.UTF_8.name());
			return result;
		} finally {
			//Clean up
			if (out != null) {
				out.close();
			}
			if (baos != null) {
				baos.close();
			}
			qout.close();
		}
	}
}
