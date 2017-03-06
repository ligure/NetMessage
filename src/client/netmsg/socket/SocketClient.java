package netmsg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.ProtocolMessage;
import netmsg.api.ClientListener;
import netmsg.base.Client;
import netmsg.utils.StringUtils;

public class SocketClient extends Thread implements Client {

    private static final Logger log = LoggerFactory
	    .getLogger(SocketClient.class);

    private boolean doDisconnect = false;
    private Socket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private ClientListener listener;

    public SocketClient(Socket socket) throws IOException {
	this.socket = socket;
	inStream = socket.getInputStream();
	outStream = socket.getOutputStream();
    }

    @Override
    public void run() {
	while (!socket.isClosed() && !doDisconnect) {
	    try {
		// socket.sendUrgentData(0xFF);
	    } catch (Exception e) {
		log.error("connection disable.");
		break;
	    }
	    try {
		int command = inStream.read();
		int length = inStream.read();
		String dataStr = "";
		if (length > 0) {
		    byte[] data = new byte[length];
		    inStream.read(data);
		    dataStr = new String(data);
		}
		log.info("服务端发送来的消息==========(" + command + ")" + dataStr);
		switch (command) {
		case ProtocolMessage.COMMAND_SET_NAME:
		    break;
		case ProtocolMessage.COMMAND_USER_LIST:
		    String[] usernames = dataStr.split(",");
		    log.info("receive user list data " + dataStr
			    + " from server.");
		    if (listener != null) {
			listener.usersUpdate(usernames);
		    }
		    break;
		case ProtocolMessage.COMMAND_SEND_MSG:
		    String[] dataSplit = dataStr.split("#");
		    log.info("message from " + dataSplit[0] + " to "
			    + dataSplit[1] + " : " + dataSplit[2]);
		    if (listener != null) {
			listener.newMessage(dataSplit[0], dataSplit[2]);
		    }
		    break;
		case ProtocolMessage.COMMAND_DISCONNECT:
		    doDisconnect = true;
		    if (listener != null) {
			listener.disconnect();
		    }
		    break;
		default:
		    String msg = StringUtils.join(' ', "unknown command code",
			    String.valueOf(command), "from server");
		    log.info(msg);
		}
	    } catch (Exception e) {
		if (!doDisconnect) {
		    doDisconnect = true;
		    log.info("connection reset.");
		    e.printStackTrace();
		}
	    }
	}
	try {
	    disconnect();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void sentData(int command, String data) throws IOException {
	if (!socket.isClosed()) {
	    byte[] msgByte = data.getBytes(Charset.defaultCharset());
	    outStream.write(command);
	    outStream.write(msgByte.length);
	    outStream.write(msgByte);
	    outStream.flush();
	} else {
	    System.out.println("connection closed.");
	}
    }

    @Override
    public void sentMessage(String from, String to, String msg)
	    throws IOException {
	log.info("a message sending from " + from + " to " + to + " : " + msg);
	String msgData = StringUtils.join('#', from, to, msg);
	sentData(ProtocolMessage.COMMAND_SEND_MSG, msgData);
    }

    @Override
    public void regUsername(String username) throws IOException {
	sentData(ProtocolMessage.COMMAND_SET_NAME, username);
    }

    @Override
    public void refreshUserList() throws IOException {
	sentData(ProtocolMessage.COMMAND_USER_LIST, "");
    }

    public static Client instance(String ip, String port) throws Exception {
	Socket s = new Socket(ip, Integer.parseInt(port));
	SocketClient sc = new SocketClient(s);
	sc.start();
	return sc;
    }

    @Override
    public void serverDisconnect() throws Exception {
	doDisconnect = true;
	sentData(ProtocolMessage.COMMAND_DISCONNECT, "");
    }

    @Override
    public void disconnect() throws Exception {
	outStream.close();
	inStream.close();
	socket.close();
	if (listener != null)
	    listener.disconnect();
    }

    @Override
    public void setListener(ClientListener cl) {
	listener = cl;
    }

    @Override
    public void connect() {
	if (listener != null)
	    listener.connected();
    }

}
