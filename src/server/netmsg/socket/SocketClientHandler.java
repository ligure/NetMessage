package netmsg.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import netmsg.ProtocolMessage;
import netmsg.base.ClientHandler;
import netmsg.base.MsgServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketClientHandler extends ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory
	    .getLogger(SocketClientHandler.class);

    private boolean doDisconnect;
    private final Socket socket;
    private final InputStream inStream;
    private final OutputStream outStream;

    public SocketClientHandler(Socket socket, MsgServer server, String clientId)
	    throws Exception {
	super(server, clientId);
	this.socket = socket;
	inStream = socket.getInputStream();
	outStream = socket.getOutputStream();
    }

    @Override
    public void run() {
	while (!socket.isClosed() && !doDisconnect) {
	    try {
		int command = inStream.read();
		int length = inStream.read();
		byte[] msgBytes = new byte[length + 2];
		msgBytes[0] = (byte) command;
		msgBytes[1] = (byte) length;
		inStream.read(msgBytes, 2, length);
		dispatch(ProtocolMessage.fromBytes(msgBytes));
	    } catch (Exception e) {
		doDisconnect = true;
		log.info("connection disable.");
		e.printStackTrace();
		break;
	    }
	}
	try {
	    disconnect();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    protected void sentData(ProtocolMessage mp) throws IOException {
	if (!socket.isClosed()) {
	    outStream.write(mp.getBytes());
	    outStream.flush();
	} else {
	    System.out.println("connection closed.");
	}
    }

    @Override
    protected void disconnect() throws Exception {
	doDisconnect = true;
	inStream.close();
	outStream.close();
	socket.close();
	getServer().removeClient(this);
    }
}
