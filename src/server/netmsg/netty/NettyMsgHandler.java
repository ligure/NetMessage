package netmsg.netty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netmsg.ProtocolMessage;
import netmsg.base.MsgServer;
import netmsg.utils.StringUtils;

public class NettyMsgHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory
	    .getLogger(NettyMsgHandler.class);

    private MsgServer server;

    Map<ChannelHandlerContext, NettyClientHandler> clientMap = new HashMap<ChannelHandlerContext, NettyClientHandler>();

    public NettyMsgHandler() {
	super();
    }

    public NettyMsgHandler(MsgServer server) {
	super();
	this.server = server;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
	InetSocketAddress isa = (InetSocketAddress) ctx.channel()
		.remoteAddress();
	String address = StringUtils.join(':', isa.getAddress().toString(),
		String.valueOf(isa.getPort()));
	NettyClientHandler ch = new NettyClientHandler(ctx, server, address);
	server.addClient(ch);
	clientMap.put(ctx, ch);
	super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
	clientMap.remove(ctx);
	super.handlerRemoved(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
	    throws Exception {
	ByteBuf msgBuf = (ByteBuf) msg;
	byte[] bytes = new byte[msgBuf.readableBytes()];
	msgBuf.readBytes(bytes);
	NettyClientHandler ch = clientMap.get(ctx);
	ch.dispatch(ProtocolMessage.fromBytes(bytes));
	super.channelRead(ctx, msgBuf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
	    throws Exception {
	ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
	ctx.close();
	logger.error("服务器异常退出" + cause.getMessage());
	cause.printStackTrace();
    }
}