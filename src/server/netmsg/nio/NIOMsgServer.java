package netmsg.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.ProtocolMessage;
import netmsg.base.ClientHandler;
import netmsg.base.MsgServer;
import netmsg.cfg.ServerConfig;
import netmsg.utils.SocketUtils;

public class NIOMsgServer extends MsgServer implements Runnable {

    private static final Logger log = LoggerFactory
	    .getLogger(NIOMsgServer.class);

    HashMap<SocketChannel, ClientHandler> clientMap = new HashMap<SocketChannel, ClientHandler>();

    @Override
    public void run() {
	try {
	    Selector selector = SelectorProvider.provider().openSelector();
	    ServerSocketChannel ssc = ServerSocketChannel.open();
	    ssc.configureBlocking(false);
	    InetSocketAddress address = new InetSocketAddress("localhost",
		    ServerConfig.SERVER_PORT);
	    ssc.socket().bind(address);
	    ssc.register(selector, SelectionKey.OP_ACCEPT);
	    while (selector.select() > 0) {
		Set<SelectionKey> keys = selector.selectedKeys();
		Iterator<SelectionKey> i = keys.iterator();
		while (i.hasNext()) {
		    SelectionKey sk = i.next();
		    i.remove();
		    if (sk.isAcceptable()) {
			accept(sk, selector);
		    }
		    if (sk.isReadable()) {
			read(sk, selector);
		    }
		    if (sk.isWritable()) {
			write(sk, selector);
		    }
		    if (sk.isConnectable()) {
			connect(sk, selector);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void connect(SelectionKey sk, Selector selector) throws Exception {

    }

    private void write(SelectionKey sk, Selector selector) throws Exception {

    }

    private void read(SelectionKey sk, Selector selector) throws Exception {
	SocketChannel sc = (SocketChannel) sk.channel();
	ArrayList<byte[]> datas = new ArrayList<byte[]>();
	ByteBuffer bb = ByteBuffer.allocate(128);
	while (sc.read(bb) > 0) {
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

	ClientHandler handler = clientMap.get(sc);
	handler.dispatch(ProtocolMessage.fromBytes(msgBytes));

    }

    private void accept(SelectionKey sk, Selector selector) throws Exception {
	ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
	SocketChannel sc = ssc.accept();
	sc.configureBlocking(false);
	log.info("client " + sc.socket().getInetAddress() + ":"
		+ sc.socket().getPort() + " connected.");
	sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ
		| SelectionKey.OP_WRITE);
	clientMap.put(sc, new NIOClientHandler(sc, this,
		SocketUtils.getRemoteSocket(sc.socket())));
	addClient(clientMap.get(sc));
    }

    public static void main(String[] args) {
	NIOMsgServer nioms = new NIOMsgServer();
	new Thread(nioms).start();

    }

    @Override
    public void removeClient(ClientHandler ch) {
	super.removeClient(ch);
	SocketChannel sc = ((NIOClientHandler) ch).getChannel();
	clientMap.remove(sc);
    }
}
