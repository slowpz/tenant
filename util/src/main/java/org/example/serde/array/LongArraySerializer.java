package org.example.serde.array;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import org.example.serde.CommonSerializer;
import org.example.serde.CommonSerializer.SerializerPair;
import org.example.serde.NettyByteBufUtil;
import org.example.serde.Serializer;

/**
 * JAVA数组序列化
 *
 *
 * <pre>
 *   一维数组:
 *
 *    元素类型|长度|元素1|元素2|
 *
 *
 *  二维数组(都压缩成一维数组)
 *
 *    长度=N|元素1|......|元素N
 *
 *
 *    长度:1-5字节, 使用varint32和ZigZag编码
 *    元素:实现决定
 * </pre>
 * <p>1.数组长宽必须一致</p>
 * <p>2.暂时不支持PrimitiveWrapper数组，序列化时会全部转化为对应的基础类型</p>
 * <p>
 * 与{@link CommonSerializer} 组合使用
 *
 * @since 2021年07月18日 14:17:04
 **/
public class LongArraySerializer implements Serializer<long[]> {



  public LongArraySerializer() {
  }


  @Override
  public long[] readObject(ByteBuf buf) {
    int length = NettyByteBufUtil.readInt32(buf);
    if (length == -1) {
      return null;
    } else {
      long[] array = new long[length];
      for (int i = 0; i < length; i++) {
        array[i] = NettyByteBufUtil.readInt64(buf);
      }
      return array;
    }
  }


  @Override
  public void writeObject(ByteBuf buf, long[] object) {
    final int length = object.length;
    NettyByteBufUtil.writeInt32(buf, length);
    for (long o : object) {
      NettyByteBufUtil.writeInt64(buf, o);
    }
  }
}