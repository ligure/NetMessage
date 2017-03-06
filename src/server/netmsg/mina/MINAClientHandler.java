package netmsg.mina;

import netmsg.ProtocolMessage;
import netmsg.base.ClientHandler;
import netmsg.base.MsgServer;

import org.apache.mina.core.session.IoSession;

public class MINAClientHandler extends ClientHandler {
    private final IoSession session;

    public MINAClientHandler(IoSession session, MsgServer server,
	    String clientId) {
	super(server, clientId);
	this.session = session;
    }

    @Override
    protected void disconnect() throws Exception {
	session.close(true);
	getServer().removeClient(this);
    }

    @Override
    protected void sentData(ProtocolMessage mp) throws Exception {
	session.write(mp);
    }

}
