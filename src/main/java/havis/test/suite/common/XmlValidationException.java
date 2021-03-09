package havis.test.suite.common;

public class XmlValidationException extends Exception {
	private static final long serialVersionUID = 7504175433949158154L;

	public XmlValidationException(String message) {
		super(message);
	}

	public XmlValidationException(String message, Throwable e) {
		super(message, e);
	}

}
