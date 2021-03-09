package havis.test.suite.common.messaging;

import java.io.Writer;

import javax.xml.stream.XMLStreamWriter;

public interface XMLStreamWriterFactory {

	XMLStreamWriter create(Writer writer);

}
