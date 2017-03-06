package netmsg.netty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import netmsg.ProtocolMessage;
import netmsg.api.ClientListener;
import netmsg.base.Client;
import netmsg.utils.StringUtils;

public class NettyClient extends Thread implements Client {

    private static final Logger log = LoggerFactory
	    .getLogger(NettyClient.class);

    private Channel channel;
    private EventLoopGroup group;
    private static ClientListener listener;
    private static List<byte[]> message = new ArrayList<byte[]>();

    public static void addMsg(byte[] bytes) {
	message.add(bytes);
    }

    public NettyClient(EventLoopGroup group, Channel channel) {
	super();
	this.group = group;
	this.channel = channel;
    }

    public void sentData(int command, String data) throws IOException {
	byte[] bytes = data.getBytes();
	ByteBuf byteBuf = channel.alloc().buffer(bytes.length + 2);
	byteBuf.writeByte((byte) command);
	byteBuf.writeByte((byte) bytes.length);
	byteBuf.writeBytes(bytes);
	channel.writeAndFlush(byteBuf);
    }

    @Override
    public void sentMessage(String from, String to, String msg)
	    throws IOException {
	log.info("a message sending from " + from + " to " + to + " : " + msg);
	String msgData = StringUtils.join('#', from, to, msg);
	sentData(ProtocolMessage.COMMAND_SEND_MSG, msgData);
    }

    @Override
    public void regUsername(String username) throws IOException {
	sentData(ProtocolMessage.COMMAND_SET_NAME, username);
    }

    @Override
    public void refreshUserList() throws IOException {
	sentData(ProtocolMessage.COMMAND_USER_LIST, "");
    }

    public static Client instance(String ip, String port) throws Exception {
	Client client = null;
	EventLoopGroup group = new NioEventLoopGroup();
	Bootstrap bootstrap = new Bootstrap();
	bootstrap.group(group).channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true) // 长连接
		.option(ChannelOption.TCP_NODELAY, true)// 不延迟，消息立即发送
		.handler(new ChannelInitializer<SocketChannel>() {
		    @Override
		    protected void initChannel(SocketChannel socketChannel)
			    throws Exception {
			socketChannel.pipeline()
				.addLast(new ChannelInboundHandlerAdapter() {
				    @Override
				    public void channelRead(
					    ChannelHandlerContext ctx,
					    Object message) throws Exception {
					ByteBuf msgBuf = (ByteBuf) message;
					byte[] bytes = new byte[msgBuf
						.readableBytes()];
					msgBuf.readBytes(bytes);
					addMsg(bytes);
					int command = bytes[0];
					int length = bytes[1];
					String dataStr = "";
					if (length > 0) {
					    dataStr = new String(
						    Arrays.copyOfRange(bytes, 2,
							    2 + length));
					}
					log.info("服务端发送来的消息==========("
						+ command + ")" + dataStr);
					switch (command) {
					case ProtocolMessage.COMMAND_SET_NAME:
					    break;
					case ProtocolMessage.COMMAND_USER_LIST:
					    String[] usernames = dataStr
						    .split(",");
					    log.info("receive user list data "
						    + dataStr
						    + " from server.");
					    if (listener != null) {
						listener.usersUpdate(usernames);
					    }
					    break;
					case ProtocolMessage.COMMAND_SEND_MSG:
					    String[] dataSplit = dataStr
						    .split("#");
					    log.info("message from "
						    + dataSplit[0] + " to "
						    + dataSplit[1] + " : "
						    + dataSplit[2]);
					    if (listener != null) {
						listener.newMessage(
							dataSplit[0],
							dataSplit[2]);
					    }
					    break;
					case ProtocolMessage.COMMAND_DISCONNECT:
					    if (listener != null) {
						listener.disconnect();
					    }
					    break;
					default:
					    String msg = StringUtils.join(' ',
						    "unknown command code",
						    String.valueOf(command),
						    "from server");
					    log.info(msg);
					}
					super.channelRead(ctx, msgBuf);
				    }

				    @Override
				    public void channelReadComplete(
					    ChannelHandlerContext ctx)
					    throws Exception {
					ctx.flush();
				    }
				});
		    }
		});
	ChannelFuture channelFuture = bootstrap.connect(ip,
		Integer.parseInt(port));
	channelFuture.awaitUninterruptibly();
	client = new NettyClient(group, channelFuture.channel());
	return client;
    }

    @Override
    public void serverDisconnect() throws Exception {
	sentData(ProtocolMessage.COMMAND_DISCONNECT, "");
    }

    @Override
    public void disconnect() throws Exception {
	group.shutdownGracefully();
	channel.close();
	if (listener != null)
	    listener.disconnect();
    }

    @Override
    public void setListener(ClientListener cl) {
	listener = cl;
    }

    @Override
    public void connect() {
	if (listener != null)
	    listener.connected();
    }

}
