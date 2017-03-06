package netmsg.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netmsg.api.ClientListener;
import netmsg.base.Client;
import netmsg.cfg.ClientConfig;
import netmsg.mina.MINAClient;
import netmsg.netty.NettyClient;
import netmsg.nio.NIOClient;
import netmsg.socket.SocketClient;
import netmsg.utils.StringUtils;

public class NetMessage extends JFrame implements ClientListener {

    private static final Logger log = LoggerFactory.getLogger(NetMessage.class);

    private static final long serialVersionUID = 1L;

    JTextField usernameBox;
    JTextField serverIPBox;
    JTextField serverPortBox;
    JButton connectButton;
    JButton nameButton;
    JButton refreshUserButton;
    JList userListBox;
    DefaultListModel<String> userListModel;
    JTextArea historyBox;
    JTextField sendBox;
    JButton sendButton;
    JButton clearHistoryButton;

    String clientType;
    Client client;

    public NetMessage(String type) {
	this.clientType = type;
	init();
    }

    public void connect() throws Exception {
	String username = usernameBox.getText();
	String ip = serverIPBox.getText();
	String port = serverPortBox.getText();

	if (StringUtils.isBlank(ip) || StringUtils.isBlank(port)) {
	    JOptionPane.showMessageDialog(this, "IP and port must be given.",
		    "error", JOptionPane.WARNING_MESSAGE);
	    return;
	}
	log.info("connecting to " + ip + ":" + port + " with name " + username);
	if ("socket".equals(clientType)) {
	    client = SocketClient.instance(ip, port);
	} else if ("nio".equals(clientType)) {
	    client = NIOClient.instance(ip, port);
	} else if ("mina".equals(clientType)) {
	    client = MINAClient.instance(ip, port);
	} else if ("netty".equals(clientType)) {
	    client = NettyClient.instance(ip, port);
	}
	client.setListener(this);
	client.connect();
	if (!StringUtils.isBlank(username))
	    client.regUsername(username);
    }

    private void init() {
	final JFrame topFrame = this;
	BorderLayout b = new BorderLayout(0, 0);
	setLayout(b);

	JPanel serverPanel = new JPanel();
	serverPanel.setBorder(BorderFactory
		.createTitledBorder(new EtchedBorder(), "Connect To"));
	add(serverPanel, BorderLayout.NORTH);
	serverPanel.setLayout(new GridBagLayout());

	usernameBox = new JTextField();
	nameButton = new JButton("OK");
	nameButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    if (client == null)
			return;
		    client.regUsername(usernameBox.getText());
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
	    }
	});
	serverIPBox = new JTextField(ClientConfig.SERVER_IP);
	serverPortBox = new JTextField(
		String.valueOf(ClientConfig.SERVER_PORT));
	connectButton = new JButton("Connect");
	connectButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		if (client == null) {
		    try {
			connect();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		} else {
		    try {
			client.serverDisconnect();
			client.disconnect();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	});

	GridBagConstraints gbcLabel = new GridBagConstraints();
	gbcLabel.gridx = 0;
	gbcLabel.anchor = GridBagConstraints.EAST;
	GridBagConstraints gbcText = new GridBagConstraints();
	gbcText.gridx = 1;
	gbcText.gridwidth = 1;
	gbcText.weightx = 1;
	gbcText.fill = GridBagConstraints.HORIZONTAL;
	GridBagConstraints gbcButton = new GridBagConstraints();
	gbcButton.gridx = 2;
	gbcButton.anchor = GridBagConstraints.WEST;

	gbcLabel.gridy = gbcText.gridy = gbcButton.gridy = 0;
	serverPanel.add(new JLabel("Name:"), gbcLabel);
	serverPanel.add(usernameBox, gbcText);
	serverPanel.add(nameButton, gbcButton);
	gbcLabel.gridy = gbcText.gridy = gbcButton.gridy = 1;
	serverPanel.add(new JLabel("IP:"), gbcLabel);
	gbcText.gridwidth = 2;
	serverPanel.add(serverIPBox, gbcText);
	gbcLabel.gridy = gbcText.gridy = gbcButton.gridy = 2;
	serverPanel.add(new JLabel("Port:"), gbcLabel);
	gbcText.gridwidth = 1;
	serverPanel.add(serverPortBox, gbcText);
	serverPanel.add(connectButton, gbcButton);

	JPanel userPanel = new JPanel(new BorderLayout());
	userPanel.setPreferredSize(new Dimension(200, 0));
	userPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),
		"Online User"));
	userListModel = new DefaultListModel<String>();
	userListBox = new JList(userListModel);
	userPanel.add(new JScrollPane(userListBox), BorderLayout.CENTER);
	refreshUserButton = new JButton("Refresh");
	refreshUserButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		try {
		    client.refreshUserList();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	});
	userPanel.add(refreshUserButton, BorderLayout.SOUTH);
	add(userPanel, BorderLayout.WEST);

	JPanel historyPanel = new JPanel();
	historyPanel.setLayout(new BorderLayout(0, 0));
	historyPanel.setBorder(BorderFactory
		.createTitledBorder(new EtchedBorder(), "History Message"));
	historyBox = new JTextArea();
	historyBox.setLineWrap(true);
	historyPanel.add(new JScrollPane(historyBox), BorderLayout.CENTER);
	add(historyPanel, BorderLayout.CENTER);
	clearHistoryButton = new JButton("Clear");
	historyPanel.add(clearHistoryButton, BorderLayout.SOUTH);
	clearHistoryButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		historyBox.setText("");
	    }
	});

	JPanel sendPanel = new JPanel();
	sendPanel.setLayout(new BorderLayout());
	add(sendPanel, BorderLayout.SOUTH);
	sendBox = new JTextField();
	sendPanel.add(sendBox, BorderLayout.CENTER);
	sendButton = new JButton("Send");
	sendPanel.add(sendButton, BorderLayout.EAST);

	sendButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		String user = (String) userListBox.getSelectedValue();
		if (user != null) {
		    try {
			client.sentMessage(usernameBox.getText(), user,
				sendBox.getText());
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		} else {
		    JOptionPane.showMessageDialog(topFrame, "choose a target!",
			    "error", JOptionPane.WARNING_MESSAGE);
		}
	    }
	});

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent event) {
		if (client != null) {
		    try {
			client.serverDisconnect();
			client.disconnect();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    }
	});

	nameButton.setEnabled(false);
	refreshUserButton.setEnabled(false);
	sendButton.setEnabled(false);
	clearHistoryButton.setEnabled(false);
    }

    protected void centerWindow() {
	Dimension dim = getToolkit().getScreenSize();
	setLocation((dim.width - getWidth()) / 2,
		(dim.height - getHeight()) / 2);
    }

    @Override
    public void connected() {
	connectButton.setText("Disconnect");
	nameButton.setEnabled(true);
	refreshUserButton.setEnabled(true);
	sendButton.setEnabled(true);
	clearHistoryButton.setEnabled(true);
    }

    @Override
    public void disconnect() {
	client = null;
	connectButton.setText("Connect");
	userListModel.clear();
	nameButton.setEnabled(false);
	refreshUserButton.setEnabled(false);
	sendButton.setEnabled(false);
	clearHistoryButton.setEnabled(false);
    }

    @Override
    public void newMessage(String from, String msg) {
	historyBox.append(
		StringUtils.join(' ', "message from", from, ":", msg, "\r\n"));
    }

    @Override
    public void usersUpdate(String[] users) {
	userListModel.clear();
	for (String user : users) {
	    userListModel.addElement(user);
	}
    }

}
