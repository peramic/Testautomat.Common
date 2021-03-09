package havis.test.suite.common;

/**
 * Determines the first difference between two strings. <example> <code>
 * var a = "abcdef";
 * var b = "abcXefghijklmn";
 * var d = new Diff(a, b);
 * var additionalChars = 1;
 * result = d.GetDiff(additionalChars);
 * // result: "- ...cde...\r\n+ ...cXe...";
 * additionalChars = 3;
 * result = d.GetDiff(additionalChars);
 * // result: ""- ...abcdef...\r\n+ ...abcXefg..."
 * </code> </example>
 */
public class Diff {

	private final String s1;
	private final String s2;

	public Diff(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	private String format(int diffIndex, int additionalCharCount) {
		if (diffIndex < 0) {
			return "";
		}

		int start = diffIndex - additionalCharCount;

		if (start < 0) {
			start = 0;
		}

		int end1 = diffIndex + additionalCharCount;
		if (end1 > s1.length() - 1) {
			end1 = s1.length() - 1;
		}
		int end2 = diffIndex + additionalCharCount;
		if (end2 > s2.length() - 1) {
			end2 = s2.length() - 1;
		}
		String ret = "";

		if (end1 >= 0) {
			ret = "- ..." + s1.substring(start, end1 + 1) + "...";
		}
		if (end2 >= 0) {
			if (ret.length() > 0) {
				ret += System.getProperty("line.separator");
			}
			ret += "+ ..." + s2.substring(start, end2 + 1) + "...";
		}
		return ret;
	}

	/**
	 * Determines the first difference. If no difference exists then null is
	 * returned
	 * 
	 * @param additionalCharCount
	 *            max. count of additional characters placed to the left side
	 *            and right side of the difference
	 * @return
	 */
	public String getDiff(int additionalCharCount) {
		// if no diff
		if (s1.equals(s2)) {
			return null;
		}

		int diffIndex = -1;
		// if one of the strings is empty
		if (s1.length() == 0 || s2.length() == 0) {
			diffIndex = 0;
		} else {
			// get index by comparing the chars
			for (int i = 0; i < s1.length() && i < s2.length(); i++) {
				if (s1.charAt(i) != s2.charAt(i)) {
					diffIndex = i;
					break;
				}
			}
		}

		// return formatted diff
		String ret = format(diffIndex, additionalCharCount);

		if (ret != null && ret.length() != 0) {
			return ret;
		} else {
			return null;
		}

	}
}
