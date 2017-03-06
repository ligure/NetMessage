package netmsg.api;

public interface ClientListener {
    void connected();

    void usersUpdate(String[] users);

    void newMessage(String from, String msg);

    void disconnect();
}
