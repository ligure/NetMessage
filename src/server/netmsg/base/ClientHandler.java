package netmsg.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.ProtocolMessage;
import netmsg.utils.StringUtils;

public abstract class ClientHandler {

    private static final Logger log = LoggerFactory
	    .getLogger(ClientHandler.class);

    private String clientId;
    private final MsgServer server;

    public ClientHandler(MsgServer server, String clientId) {
	this.server = server;
	this.clientId = clientId;
    }

    public String getClientId() {
	return clientId;
    }

    public void sendUserList(String usernames) throws Exception {
	sentData(ProtocolMessage.createUserList(usernames));
    }

    public void sentMessage(String from, String msg) throws Exception {
	sentData(ProtocolMessage.createSendMessage(from, clientId, msg));
    }

    public MsgServer getServer() {
	return server;
    }

    public void dispatch(ProtocolMessage mp) throws Exception {
	String msg = mp.message();
	switch (mp.command()) {
	case ProtocolMessage.COMMAND_SET_NAME:
	    log.info("user " + clientId + " set name with " + msg);
	    if (StringUtils.isBlank(msg)) {

	    } else {
		clientId = msg;
	    }
	    getServer().broadcastUserList();
	    break;
	case ProtocolMessage.COMMAND_USER_LIST:
	    log.info("user " + clientId + " request for user list.");
	    sendUserList(getServer().usernames());
	    break;
	case ProtocolMessage.COMMAND_SEND_MSG:
	    String[] dataSplit = msg.split("#");
	    log.info("a message sending from " + clientId + " to "
		    + dataSplit[1] + " : " + dataSplit[2]);
	    if ("_ssd_".equals(dataSplit[2])) {
		getServer().shutdown();
	    } else
		getServer().sentMessage(clientId, dataSplit[1], dataSplit[2]);
	    break;
	case ProtocolMessage.COMMAND_DISCONNECT:
	    disconnect();
	    server.broadcastUserList();
	    log.info("client " + clientId + " #" + clientId
		    + " request for logging out.");
	    break;
	default:
	    String errMsg = StringUtils.join(' ', "unknown command code",
		    String.valueOf(mp.command()), "from user", clientId);
	    log.info(errMsg);
	    sentMessage("server", errMsg);
	}
    }

    protected void disconnectClient() throws Exception {
	sentData(ProtocolMessage.createDisconnect());
    }

    protected abstract void sentData(ProtocolMessage mp) throws Exception;

    protected abstract void disconnect() throws Exception;

}