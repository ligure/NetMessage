package netmsg.gui;

import javax.swing.UIManager;

public class SocketClientWindow extends NetMessage {

    private static final long serialVersionUID = 2078404343820656264L;

    public SocketClientWindow(String type) {
	super(type);
    }

    public static void main(String[] args) {
	try {
	    String lafClass = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
	    UIManager.setLookAndFeel(lafClass);
	} catch (Exception e) {
	    try {
		UIManager.setLookAndFeel(
			UIManager.getSystemLookAndFeelClassName());
	    } catch (Exception ee) {
		System.out.println("Error setting native LAF: " + ee);
	    }
	}
	SocketClientWindow nmc = new SocketClientWindow("socket");
	nmc.setTitle("SocketClientWindow");
	nmc.setSize(600, 500);
	nmc.centerWindow();
	nmc.setDefaultCloseOperation(EXIT_ON_CLOSE);
	// nmc.setIconImage(logo.getImage());
	nmc.setVisible(true);
    }

}
