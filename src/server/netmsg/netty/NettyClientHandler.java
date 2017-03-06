package netmsg.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import netmsg.ProtocolMessage;
import netmsg.base.ClientHandler;
import netmsg.base.MsgServer;

public class NettyClientHandler extends ClientHandler {

    private final ChannelHandlerContext ctx;

    public NettyClientHandler(ChannelHandlerContext ctx, MsgServer server,
	    String clientId) {
	super(server, clientId);
	this.ctx = ctx;
    }

    @Override
    protected void disconnect() throws Exception {
	ctx.close();
	getServer().removeClient(this);
    }

    @Override
    protected void sentData(ProtocolMessage mp) throws Exception {
	byte[] bytes = mp.getBytes();
	ByteBuf byteBuf = ctx.alloc().buffer(bytes.length);
	byteBuf.writeBytes(bytes);
	ctx.writeAndFlush(byteBuf);
    }

}
