package netmsg.base;

import java.io.IOException;

import netmsg.api.ClientListener;

public interface Client {

    public abstract void connect();

    public abstract void setListener(ClientListener cl);

    public abstract void disconnect() throws Exception;

    public abstract void serverDisconnect() throws Exception;

    public abstract void refreshUserList() throws IOException;

    public abstract void regUsername(String username) throws IOException;

    public abstract void sentMessage(String from, String to, String msg)
	    throws IOException;

}
