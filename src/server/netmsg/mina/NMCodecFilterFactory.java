package netmsg.mina;

import netmsg.ProtocolMessage;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class NMCodecFilterFactory implements ProtocolCodecFactory {

    public class MessageDecoder extends ProtocolDecoderAdapter {
	@Override
	public void decode(IoSession session, IoBuffer buffer,
		ProtocolDecoderOutput output) throws Exception {

	    int length = buffer.limit();
	    byte[] bytes = new byte[length];
	    buffer.get(bytes);
	    output.write(ProtocolMessage.fromBytes(bytes));
	}
    }

    public class MessageEncoder extends ProtocolEncoderAdapter {
	@Override
	public void encode(IoSession session, Object message,
		ProtocolEncoderOutput output) throws Exception {
	    ProtocolMessage mp = (ProtocolMessage) message;
	    byte[] bytes = mp.getBytes();
	    IoBuffer buffer = IoBuffer.allocate(bytes.length);
	    buffer.put(bytes);
	    buffer.flip();
	    output.write(buffer);
	}
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
	return new NMCodecFilterFactory.MessageDecoder();
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
	return new NMCodecFilterFactory.MessageEncoder();
    }

}
