package netmsg.utils;

public class StringUtils {

    public static String join(char splitor, String... strings) {
	StringBuffer s = new StringBuffer();
	for (int i = 0; i < strings.length; i++) {
	    s.append(strings[i]);
	    if (i < strings.length - 1)
		s.append(splitor);
	}
	return s.toString();
    }

    public static String join(String... strings) {
	return join(',', strings);
    }

    public static boolean isBlank(String s) {
	if (s == null)
	    return true;
	if ("".equals(s.trim()))
	    return true;
	return false;
    }

    public static void main(String[] args) {
	System.out.println(join('#', "aaa", "bbb", "cccc"));
    }
}
