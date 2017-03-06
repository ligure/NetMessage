package netmsg.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import netmsg.ProtocolMessage;
import netmsg.base.ClientHandler;
import netmsg.base.MsgServer;

public class NIOClientHandler extends ClientHandler {
    private final SocketChannel channel;

    public NIOClientHandler(SocketChannel sc, MsgServer server, String clientId) {
	super(server, clientId);
	channel = sc;
    }

    protected void sentData(ProtocolMessage mp) throws IOException {
	byte[] bytes = mp.getBytes();
	ByteBuffer bb = ByteBuffer.allocateDirect(bytes.length);
	bb.put(bytes);
	bb.flip();
	try {
	    channel.write(bb);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public SocketChannel getChannel() {
	return channel;
    }

    @Override
    protected void disconnect() throws Exception {
	channel.close();
	getServer().removeClient(this);
    }
}
