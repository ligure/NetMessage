package netmsg.mina;

import java.net.InetSocketAddress;

import netmsg.base.MsgServer;
import netmsg.cfg.ServerConfig;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MINAMsgServer extends MsgServer implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MINAMsgServer.class);

    public static void main(String[] args) {
	MINAMsgServer server = new MINAMsgServer();
	new Thread(server).start();
    }

    @Override
    public void run() {
	try {
	    IoAcceptor acceptor = new NioSocketAcceptor();
	    acceptor.getFilterChain().addLast("codec",
		    new ProtocolCodecFilter(new NMCodecFilterFactory()));
	    acceptor.getSessionConfig().setReadBufferSize(2048);
	    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
	    acceptor.setHandler(new MsgIoHandler(this));
	    acceptor.bind(new InetSocketAddress(ServerConfig.SERVER_PORT));
	    logger.info("服务端启动成功，端口号为：" + ServerConfig.SERVER_PORT);
	} catch (Exception e) {
	    logger.error("服务端启动异常。", e);
	    e.printStackTrace();
	}
    }

}
