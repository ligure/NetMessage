package netmsg.base;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.cfg.ServerConfig;
import netmsg.utils.StringUtils;

public class MsgServer {

    private static final Logger log = LoggerFactory.getLogger(MsgServer.class);

    private Set<ClientHandler> clients;

    public MsgServer() {
	clients = new HashSet<ClientHandler>();
    }

    protected void sentMessage(String from, String to, String msg) {
	ClientHandler fromch = searchUser(from);
	ClientHandler toch = searchUser(to);
	if (toch == null) {
	    sentMessage("server", fromch.getClientId(),
		    "cannot find connected user with name" + to);
	} else {
	    try {
		toch.sentMessage(fromch.getClientId(), msg);
	    } catch (Exception e) {
		log.info("error send message from " + fromch.getClientId()
			+ " to " + to);
		e.printStackTrace();
	    }
	}
    }

    private ClientHandler searchUser(String to) {
	for (ClientHandler ch : clients) {
	    if (to.equals(ch.getClientId()))
		return ch;
	}
	return null;
    }

    public void addClient(ClientHandler ch) {
	log.info("client " + ch.getClientId() + " connected " + ".");
	clients.add(ch);
	broadcastUserList();
	try {
	    ch.sentMessage("server",
		    StringUtils.join(' ', ServerConfig.WELCOME_MSG, "!"));
	} catch (Exception e) {
	    log.info("send welcom message to " + ch.getClientId() + " error.");
	    e.printStackTrace();
	}
    }

    public void removeClient(ClientHandler ch) {
	clients.remove(ch);
	broadcastUserList();
    }

    protected void broadcastUserList() {
	String usernames = usernames();
	for (ClientHandler sch : clients) {
	    try {
		sch.sendUserList(usernames);
	    } catch (Exception e) {
		log.info("send user list to " + sch.getClientId() + " error");
		e.printStackTrace();
	    }
	}
    }

    protected String usernames() {
	int size = clients.size();
	int count = 0;
	StringBuffer usernames = new StringBuffer();
	for (ClientHandler sch : clients) {
	    count++;
	    usernames.append(sch.getClientId());
	    if (count < size) {
		usernames.append(",");
	    }
	}
	return usernames.toString();
    }

    public void shutdown() {
	beforeShutdown();
	for (ClientHandler sch : clients) {
	    try {
		sch.disconnectClient();
		sch.disconnect();
		removeClient(sch);
		log.info("client " + sch.getClientId() + " #"
			+ sch.getClientId() + " disconnecting...");
	    } catch (Exception e) {
		log.info("client " + sch.getClientId() + " #"
			+ sch.getClientId() + " disconnect error.");
		e.printStackTrace();
	    }
	}
	afterShutdown();
    }

    protected void beforeShutdown() {

    }

    protected void afterShutdown() {

    }
}