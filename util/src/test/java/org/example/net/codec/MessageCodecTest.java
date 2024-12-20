package org.example.net.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import org.example.net.Message;
import org.example.serde.NettyByteBufUtil;
import org.junit.jupiter.api.Test;

public class MessageCodecTest {

  @Test
  public void outBoundTest() {
    EmbeddedChannel channel = new EmbeddedChannel(new MessageCodec());

    int protoId = ThreadLocalRandom.current().nextInt();
    Message message = Message.of(
        protoId,
        "Hello World".getBytes(StandardCharsets.UTF_8)
    );
    channel.writeOutbound(message);
    assertEquals(message.refCnt(), 0);

    ByteBuf out = channel.readOutbound();

    int length = out.readInt();
    assertTrue(0 < length);

    assertEquals(protoId, NettyByteBufUtil.readInt32(out));
    byte[] bytes = new byte[out.readableBytes()];
    out.readBytes(bytes);
    assertEquals(Unpooled.wrappedBuffer("Hello World".getBytes(StandardCharsets.UTF_8)),
        Unpooled.wrappedBuffer(bytes));
    assertEquals(0, out.readableBytes());

    channel.finishAndReleaseAll();
  }

  @Test
  public void inBoundTest() {
    EmbeddedChannel channel = new EmbeddedChannel(new MessageCodec());

    int proto = ThreadLocalRandom.current().nextInt();
    int optIdx = ThreadLocalRandom.current().nextInt();
    byte[] helloWorld = "Hello World".getBytes(StandardCharsets.UTF_8);

    ByteBuf inBuf = Unpooled.buffer();
    int start = inBuf.writerIndex();
    inBuf.writerIndex(Integer.BYTES);
    NettyByteBufUtil.writeInt32(inBuf, proto);
    NettyByteBufUtil.writeInt32(inBuf, optIdx);
    inBuf.writeBytes(helloWorld);

    inBuf.setInt(start, inBuf.readableBytes());
    channel.writeInbound(inBuf);

    Message out = channel.readInbound();
    assertEquals(proto, out.proto());
    assertEquals(optIdx, NettyByteBufUtil.readInt32(out.packet()));
    assertEquals(Unpooled.wrappedBuffer(helloWorld), out.packet());
    assertEquals(0, inBuf.readableBytes());

    channel.finishAndReleaseAll();
  }


}
