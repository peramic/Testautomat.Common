package havis.test.suite.common.helpers;

public class ResultCompareHelper {
	
	public static void printBytesHex(String a, String b)
	{
		for (Byte c : a.getBytes()) {
			System.out.print(Integer.toHexString(c) + "|");
		}
		System.out.println("");
		System.out.println("################");
		for (Byte c : b.getBytes()) {
			System.out.print(Integer.toHexString(c) + "|");
		}
		System.out.println("");
		System.out.println("################");
	}

}
