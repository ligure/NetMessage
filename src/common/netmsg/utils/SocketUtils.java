package netmsg.utils;

import java.net.Socket;

public class SocketUtils {

    public static String getRemoteAddress(Socket s) {
	return s.getInetAddress().getHostAddress();
    }

    public static int getRemotePort(Socket s) {
	return s.getPort();
    }

    public static String getRemoteSocket(Socket s) {
	return StringUtils.join(':', getRemoteAddress(s),
		String.valueOf(getRemotePort(s)));
    }
}
