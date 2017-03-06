package netmsg.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.ProtocolMessage;
import netmsg.api.ClientListener;
import netmsg.base.Client;
import netmsg.utils.StringUtils;

public class NIOClient extends Thread implements Client {

    private static final Logger log = LoggerFactory.getLogger(NIOClient.class);

    private boolean doDisconnect = false;
    private Selector selector;
    private SocketChannel channel;
    private ClientListener listener;

    public NIOClient(SocketChannel channel, Selector selector)
	    throws IOException {
	this.channel = channel;
	this.selector = selector;
    }

    @Override
    public void run() {
	try {
	    SelectionKey key;
	    while (selector.select() > 0 && !doDisconnect) {
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		for (Iterator<SelectionKey> i = selectedKeys.iterator(); i
			.hasNext();) {
		    key = i.next();
		    i.remove();
		    if (key.isReadable()) {
			byte[] msgByte = read(channel);
			int command = msgByte[0];
			int length = msgByte[1];
			String dataStr = "";
			if (length > 0) {
			    dataStr = new String(
				    Arrays.copyOfRange(msgByte, 2, 2 + length));
			}
			log.info("服务端发送来的消息==========(" + command + ")"
				+ dataStr);
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
			    log.info("message from " + dataSplit[0] + " to ",
				    dataSplit[1] + " : ", dataSplit[2]);
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
			    String msg = StringUtils.join(' ',
				    "unknown command code",
				    String.valueOf(command), "from server");
			    log.info(msg);
			}
		    }
		}
	    }
	} catch (Exception e) {
	    if (!doDisconnect) {
		doDisconnect = true;
		log.info("connection reset.");
		e.printStackTrace();
	    }
	}
    }

    private byte[] read(SocketChannel channel) throws Exception {
	ArrayList<byte[]> datas = new ArrayList<byte[]>();
	ByteBuffer bb = ByteBuffer.allocate(128);
	while (channel.read(bb) > 0) {
	    bb.flip();
	    datas.add(Arrays.copyOf(bb.array(), bb.limit()));
	}
	int length = (datas.size() - 1) * 128
		+ datas.get(datas.size() - 1).length;
	byte[] msgBytes = new byte[length];
	int position = 0;
	for (byte[] data : datas) {
	    System.arraycopy(data, 0, msgBytes, position, data.length);
	    position += data.length;
	}
	return msgBytes;
    }

    public void sentData(int command, String data) throws IOException {
	byte[] msgByte = data.getBytes(Charset.defaultCharset());
	ByteBuffer buff = ByteBuffer.allocateDirect(msgByte.length + 2);
	buff.put((byte) command);
	buff.put((byte) msgByte.length);
	buff.put(msgByte);
	buff.flip();
	channel.write(buff);
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
	SocketChannel channel = SocketChannel
		.open(new InetSocketAddress(ip, Integer.parseInt(port)));
	channel.configureBlocking(false);
	Selector selector = SelectorProvider.provider().openSelector();
	channel.register(selector,
		SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	NIOClient client = new NIOClient(channel, selector);
	client.start();
	return client;
    }

    @Override
    public void serverDisconnect() throws Exception {
	doDisconnect = true;
	sentData(ProtocolMessage.COMMAND_DISCONNECT, "");
    }

    @Override
    public void disconnect() throws Exception {
	channel.close();
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
