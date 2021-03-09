package havis.test.suite.common.messaging;

import havis.test.suite.testcase.ImportType;
import havis.test.suite.testcase.StepType;
import havis.test.suite.testcase.StepsType;
import havis.test.suite.testcase.TestCaseType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLMessageTest {

	private TestCaseType testcase = null;

	public class SampleRequest {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@BeforeClass
	public void setUp() {
		testcase = new TestCaseType();
		StepsType steps = new StepsType();
		ArrayList<StepType> step = new ArrayList<StepType>();
		step.add(new StepType());
		step.get(0).setName("Name1");
		step.get(0).setComment("Test special chars: äöüß");
		ImportType imPort = new ImportType();
		imPort.setTestCaseURI("Import1");
		step.get(0).setImport(imPort);
		steps.getStep().addAll(step);
		testcase.setSteps(steps);
	}

	@Test
	public void getSerializationString() throws IOException, JAXBException {
		XMLMessage msg = new XMLMessage((SampleRequest) null);
		Assert.assertNull(msg.getSerializationString("", ""));

		SampleRequest sampleRequest = new SampleRequest();
		sampleRequest.setName("ä" + System.getProperty("line.separator") + "a");
		msg = new XMLMessage(sampleRequest);

		// Missing Jaxb binding
		try {
			msg.getSerializationString("", "");
			Assert.fail();
		} catch (JAXBException e) {
			Assert.assertTrue(e.getMessage().contains(
					"IllegalAnnotationExceptions"));
		} catch (Exception e) {
			Assert.fail();
		}

		// Missing required element
		StepsType buf = testcase.getSteps();
		testcase.setSteps(null);
		msg = new XMLMessage(testcase);
		try {
			msg.getSerializationString("", "");
			//TODO No exception?
			// Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("Missing"));
		} catch (Exception e) {
			Assert.fail();
		}
		testcase.setSteps(buf);

		// Missing required parameter
		String name = testcase.getSteps().getStep().get(0).getName();
		testcase.getSteps().getStep().get(0).setName(null);
		msg = new XMLMessage(testcase);
		try {
			msg.getSerializationString("", "");
			//TODO No exception?
			//Assert.fail();
		} catch (JAXBException e) {
			Assert.assertTrue(e.getMessage().contains("null value"));
		} catch (Exception e) {
			Assert.fail();
		}
		testcase.getSteps().getStep().get(0).setName(name);

		msg = new XMLMessage(testcase);

		byte[] ansiUnExpected = getTestCaseXml().getBytes(
				StandardCharsets.ISO_8859_1);
		byte[] utf8Expected = getTestCaseXml().getBytes(StandardCharsets.UTF_8);
		byte[] utf8Actual = msg.getSerializationString("testCase", "http://www.HARTING.com/RFID/TestAutomat").getBytes(
				StandardCharsets.UTF_8);

		// Wrong Encoding
		Assert.assertNotEquals(utf8Actual.length, ansiUnExpected.length);
		// Correct Encoding
		Assert.assertEquals(utf8Actual, utf8Expected);
		// Result String o.k.
		Assert.assertEquals(msg.getSerializationString("testCase", "http://www.HARTING.com/RFID/TestAutomat"),
				getTestCaseXml());
	}

	@Test
	public void getDeserializedObject() throws JAXBException, IOException,
			JAXBException {

		// Parameter null
		XMLMessage obj = new XMLMessage((String) null, null);
		Assert.assertNull(obj.getDeserializedObject());
		obj = new XMLMessage((String) null, String.class);
		Assert.assertNull(obj.getDeserializedObject());
		obj = new XMLMessage(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><a></a>", null);
		Assert.assertNull(obj.getDeserializedObject());

		// Missing binding to object (not serializable)
		obj = new XMLMessage(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><a></a>",
				SampleRequest.class);
		try {
			obj.getDeserializedObject();
			Assert.fail();
		} catch (JAXBException e) {
			Assert.assertTrue(e.getMessage().contains(
					"IllegalAnnotationExceptions"));
		} catch (Exception e) {
			Assert.fail();
		}

		// Correct deserialization
		obj = new XMLMessage(getTestCaseXml(), TestCaseType.class);
		XMLMessage xml = new XMLMessage(obj.getDeserializedObject());
		Assert.assertEquals(xml.getSerializationString("testCase",
				"http://www.HARTING.com/RFID/TestAutomat"), getTestCaseXml());
	}

	@Test
	public void normalize() throws XMLNormalizationException,
			ParserConfigurationException, SAXException, IOException {
		// Null message
		XMLMessage msg = new XMLMessage(null);
		Assert.assertNull(msg.normalize());

		// Empty message
		msg = new XMLMessage("    ");
		try {
			msg.normalize();
			Assert.fail();
		} catch (SAXParseException e) {
			Assert.assertTrue(e.toString().contains("lineNumber"));
		} catch (Exception e) {
			Assert.fail();
		}

		// XML-Declaration only
		msg = new XMLMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		try {
			msg.normalize();
			Assert.fail();
		} catch (SAXParseException e) {
			Assert.assertTrue(e.toString().contains("lineNumber"));
		} catch (Exception e) {
			Assert.fail();
		}

		// Missing end tag
		msg = new XMLMessage("<a>");
		try {
			msg.normalize();
			Assert.fail();
		} catch (SAXParseException e) {
			Assert.assertTrue(e.toString().contains("lineNumber"));
		} catch (Exception e) {
			Assert.fail();
		}

		// Correct XML with XML-Declaration
		msg = new XMLMessage(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <a></a>  ");
		Assert.assertEquals(
				msg.normalize(),
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ System.getProperty("line.separator") + "<a/>"
						+ System.getProperty("line.separator"));

		// Correct XML without XML-Declaration
		msg = new XMLMessage("" + System.getProperty("line.separator")
				+ "<a><b><c>c</c></b>" + System.getProperty("line.separator")
				+ "  </a> ");
		Assert.assertEquals(
				msg.normalize(),
				String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%1$s"
						+ "<a>%1$s" + "    <b>%1$s" + "        <c>c</c>%1$s"
						+ "    </b>%1$s" + "</a>%1$s",
						System.getProperty("line.separator")));

		// Correct XML with XML-Declaration
		msg = new XMLMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  "
				+ System.getProperty("line.separator") + "<a><b><c>c</c></b>"
				+ System.getProperty("line.separator") + "  </a> ");
		Assert.assertEquals(
				msg.normalize(),
				String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%1$s"
						+ "<a>%1$s" + "    <b>%1$s" + "        <c>c</c>%1$s"
						+ "    </b>%1$s" + "</a>%1$s",
						System.getProperty("line.separator")));

	}

	public String getTestCaseXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<testCase xmlns=\"http://www.HARTING.com/RFID/TestAutomat\">"
				+ "<steps><step name=\"Name1\"><comment>Test special chars: äöüß</comment>"
				+ "<import><testCaseURI>Import1</testCaseURI></import></step></steps>"
				+ "</testCase>";
	}

}
