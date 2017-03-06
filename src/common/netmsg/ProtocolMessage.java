package netmsg;

import java.nio.charset.Charset;
import java.util.Arrays;

import netmsg.utils.StringUtils;

public class ProtocolMessage {
    public static final int COMMAND_SET_NAME = 0;
    public static final int COMMAND_SEND_MSG = 2;
    public static final int COMMAND_USER_LIST = 1;
    public static final int COMMAND_DISCONNECT = 9;

    private int command;
    private String message;

    private ProtocolMessage() {
    }

    private ProtocolMessage(int command, String message) {
	this.command = command;
	this.message = message;
    }

    public int command() {
	return command;
    }

    public String message() {
	if (message != null)
	    return message;
	else
	    return "";
    }

    public byte[] getBytes() {
	byte[] msgBytes = message().getBytes(Charset.defaultCharset());
	int length = msgBytes.length;
	byte[] bytes = new byte[length + 2];
	bytes[0] = (byte) command();
	bytes[1] = (byte) length;
	System.arraycopy(msgBytes, 0, bytes, 2, length);
	return bytes;
    }

    public static ProtocolMessage fromBytes(byte[] bytes) {
	ProtocolMessage pm = new ProtocolMessage();
	pm.command = bytes[0];
	int length = bytes[1];
	if (length > 0) {
	    pm.message = new String(Arrays.copyOfRange(bytes, 2, 2 + length),
		    Charset.defaultCharset());
	}
	return pm;
    }

    public static ProtocolMessage createSetName(String name) {
	return new ProtocolMessage(ProtocolMessage.COMMAND_SET_NAME, name);
    }

    public static ProtocolMessage createSendMessage(String from, String to,
	    String msg) {
	String msgData = StringUtils.join('#', from, to, msg);
	return new ProtocolMessage(ProtocolMessage.COMMAND_SEND_MSG, msgData);
    }

    public static ProtocolMessage createUserList(String usernames) {
	return new ProtocolMessage(ProtocolMessage.COMMAND_USER_LIST, usernames);
    }

    private static final ProtocolMessage disconnectInstance = new ProtocolMessage(
	    ProtocolMessage.COMMAND_DISCONNECT, "");

    public static ProtocolMessage createDisconnect() {
	return disconnectInstance;
    }
}
