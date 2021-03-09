package havis.test.suite.common.messaging;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Serializer to parse an Exception to a String or to a XML String
 * 
 */
public class ExceptionSerializer {

	/**
	 * Parse Exception Stacktrace to a String
	 * @param e
	 * @return
	 * @throws IOException
	 */
	public static String getExceptionStackString(Throwable e)
			throws IOException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			e.printStackTrace(pw);
		} finally {
			if (sw != null) {
				sw.close();
			}

			if (pw != null) {
				pw.close();
			}
		}
		return sw.toString();
	}

	/**
	 * Parse Exception Stacktrace to a XML String
	 * Current form <stacktrace>e.stackTrace<stacktrace>
	 * @param e
	 * @return
	 * @throws IOException
	 */
	public static String getExceptionStackXml(Throwable e) throws IOException {
		String exceptionStack = getExceptionStackString(e);
		return "<stacktrace><![CDATA[" + exceptionStack + "]]></stacktrace>";
	}

}
