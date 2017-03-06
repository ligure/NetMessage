package netmsg.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.ProtocolMessage;
import netmsg.api.ClientListener;
import netmsg.base.Client;
import netmsg.utils.StringUtils;

public class MINAClient extends Thread implements Client {

    private static final Logger log = LoggerFactory.getLogger(MINAClient.class);

    private IoSession session;
    private static ClientListener listener;
    private static List<byte[]> message = new ArrayList<byte[]>();

    public static void addMsg(byte[] bytes) {
	message.add(bytes);
    }

    public MINAClient(IoSession session) {
	this.session = session;
    }

    public void sentData(int command, String data) throws IOException {
	byte[] msgByte = data.getBytes(Charset.defaultCharset());
	IoBuffer buffer = IoBuffer.allocate(msgByte.length + 2);
	buffer.put((byte) command);
	buffer.put((byte) msgByte.length);
	buffer.put(msgByte);
	buffer.flip();
	session.write(buffer);
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

    public static Client instance(String ip, String port) throws IOException {
	NioSocketConnector connector = new NioSocketConnector();
	connector.setHandler(new IoHandlerAdapter() {
	    @Override
	    public void messageReceived(IoSession session, Object message)
		    throws Exception {
		IoBuffer buffer = (IoBuffer) message;
		byte[] bytes = new byte[buffer.limit()];
		buffer.get(bytes);
		addMsg(bytes);
		int command = bytes[0];
		int length = bytes[1];
		String dataStr = "";
		if (length > 0) {
		    dataStr = new String(
			    Arrays.copyOfRange(bytes, 2, 2 + length));
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
		    if (listener != null) {
			listener.disconnect();
		    }
		    break;
		default:
		    String msg = StringUtils.join(' ', "unknown command code",
			    String.valueOf(command), "from server");
		    log.info(msg);
		}
		super.messageReceived(session, message);
	    }
	});
	ConnectFuture future = connector
		.connect(new InetSocketAddress(ip, Integer.parseInt(port)));
	future.awaitUninterruptibly();
	MINAClient client = new MINAClient(future.getSession());
	return client;
    }

    @Override
    public void serverDisconnect() throws Exception {
	sentData(ProtocolMessage.COMMAND_DISCONNECT, "");
    }

    @Override
    public void disconnect() throws Exception {
	session.close(true);
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
