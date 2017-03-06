package netmsg.mina;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import netmsg.ProtocolMessage;
import netmsg.base.MsgServer;
import netmsg.utils.StringUtils;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class MsgIoHandler extends IoHandlerAdapter {

    MsgServer server;
    Map<IoSession, MINAClientHandler> clientMap = new HashMap<IoSession, MINAClientHandler>();

    public MsgIoHandler() {
	super();
    }

    public MsgIoHandler(MsgServer ms) {
	super();
	this.server = ms;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
	    throws Exception {
	cause.printStackTrace();
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
	InetSocketAddress isa = (InetSocketAddress) session.getRemoteAddress();
	String address = StringUtils.join(':', isa.getAddress().toString(),
		String.valueOf(isa.getPort()));
	MINAClientHandler ch = new MINAClientHandler(session, server, address);
	server.addClient(ch);
	clientMap.put(session, ch);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
	clientMap.remove(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
	    throws Exception {
	ProtocolMessage recvMessage = (ProtocolMessage) message;
	MINAClientHandler ch = clientMap.get(session);
	ch.dispatch(recvMessage);
    }

}
