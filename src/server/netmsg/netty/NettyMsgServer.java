package netmsg.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import netmsg.base.MsgServer;
import netmsg.cfg.ServerConfig;

public class NettyMsgServer extends MsgServer implements Runnable {

    private static Logger logger = LoggerFactory
	    .getLogger(NettyMsgServer.class);

    @Override
    public void run() {
	EventLoopGroup boss = new NioEventLoopGroup();
	EventLoopGroup worker = new NioEventLoopGroup();
	ServerBootstrap bootstrap = new ServerBootstrap();
	bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
		.option(ChannelOption.SO_BACKLOG, 1024)// 连接数
		.childOption(ChannelOption.SO_KEEPALIVE, true) // 长连接
		.childOption(ChannelOption.TCP_NODELAY, true) // 不延迟，消息立即发送
		.childHandler(new ChannelInitializer<SocketChannel>() {
		    @Override
		    protected void initChannel(SocketChannel channel)
			    throws Exception {
			channel.pipeline().addLast(
				new NettyMsgHandler(NettyMsgServer.this));
		    }
		});
	ChannelFuture channelFuture = null;
	try {
	    channelFuture = bootstrap.bind(ServerConfig.SERVER_PORT).sync();
	    if (channelFuture.isSuccess()) {
		logger.info("服务端启动成功，端口号为：" + ServerConfig.SERVER_PORT);
	    }
	    channelFuture.channel().closeFuture().sync();
	} catch (InterruptedException e) {
	    logger.error("服务端启动异常。", e);
	    e.printStackTrace();
	} finally {
	    boss.shutdownGracefully();
	    worker.shutdownGracefully();
	    logger.info("server已关闭");
	}
    }

    public static void main(String[] args) {
	NettyMsgServer nettySrv = new NettyMsgServer();
	new Thread(nettySrv).start();
    }

}
