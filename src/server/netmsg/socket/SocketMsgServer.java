package netmsg.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import netmsg.base.MsgServer;
import netmsg.cfg.ServerConfig;
import netmsg.utils.SocketUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketMsgServer extends MsgServer implements Runnable {

    private static final Logger log = LoggerFactory
	    .getLogger(SocketMsgServer.class);

    private boolean doShutdown = false;
    private ServerSocket serverSocket;

    public SocketMsgServer() throws IOException {
	serverSocket = new ServerSocket(ServerConfig.SERVER_PORT);
    }

    @Override
    public void run() {
	log.info("server started.");
	while (!doShutdown) {
	    try {
		Socket socket = serverSocket.accept();
		SocketClientHandler sch = new SocketClientHandler(socket, this,
			SocketUtils.getRemoteSocket(socket));
		new Thread(sch).start();
		addClient(sch);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public void beforeShutdown() {
	doShutdown = true;
    }

    public static void main(String[] args) throws IOException {
	SocketMsgServer sms = new SocketMsgServer();
	new Thread(sms).start();
    }

}
