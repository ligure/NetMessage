package netmsg.gui;

import javax.swing.UIManager;

public class MINAClientWindow extends NetMessage {

    private static final long serialVersionUID = 2078404343820656264L;

    public MINAClientWindow(String type) {
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
	MINAClientWindow nmc = new MINAClientWindow("mina");
	nmc.setTitle("MINAClientWindow");
	nmc.setSize(600, 500);
	nmc.centerWindow();
	nmc.setDefaultCloseOperation(EXIT_ON_CLOSE);
	// nmc.setIconImage(logo.getImage());
	nmc.setVisible(true);
    }

}
