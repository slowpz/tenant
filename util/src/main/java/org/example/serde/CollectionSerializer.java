package org.example.serde;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.IntFunction;
import org.example.serde.CommonSerializer.SerializerPair;


/**
 * 通用集合序列化，默认实现为{@link ArrayList}
 * <p>
 *
 * <pre>
 *   一维数组:
 *
 *    元素数量|唯一类型ID(非0需处理)|元素1|元素2|元素3|元素3|
 *
 *    元素数量:1-5字节, 使用varint32和ZigZga编码
 *    元素:实现决定
 * </pre>
 * <p>
 * 与{@link CommonSerializer} 组合使用
 *
 * @since 2021年07月18日 14:17:04
 **/
public class CollectionSerializer implements Serializer<Object> {

  /**
   * 集合提供者
   */
  private IntFunction<Collection<Object>> factory;

  public CollectionSerializer() {
    this(ArrayList::new);
  }

  /**
   * @param factory 根据长度创建一个集合
   * @since 2024/8/8 22:36
   */
  public CollectionSerializer(IntFunction<Collection<Object>> factory) {
    this.factory = factory;
  }

  @Override
  public Object readObject(CommonSerializer serializer, ByteBuf buf) {
    int length = NettyByteBufUtil.readInt32(buf);
    if (length < 0) {
      return null;
    }

    int typeId = NettyByteBufUtil.readInt32(buf);

    Collection<Object> collection = factory.apply(length);

    if (typeId != 0) {
      Serializer<Object> ser = null;
      Class<?> clz = Objects.requireNonNull(serializer.getClazz(typeId),
          () -> "未注册的类型ID:%s".formatted(typeId));
      SerializerPair pair = Objects.requireNonNull(serializer.getSerializerPair(clz),
          () -> "未注册的类型:%s".formatted(clz));
      ser = (Serializer<Object>) pair.serializer();

      for (int i = 0; i < length; i++) {
        collection.add(ser.readObject(serializer, buf));
      }
    } else {
      for (int i = 0; i < length; i++) {
        collection.add(serializer.readObject(buf));
      }
    }

    return collection;
  }

  @Override
  public void writeObject(CommonSerializer serializer, ByteBuf buf, Object object) {
    if (object == null) {
      NettyByteBufUtil.writeInt32(buf, -1);
    } else {
      if (!(object instanceof Collection)) {
        throw new RuntimeException("类型:" + object.getClass() + ",不是集合");
      }
      @SuppressWarnings("unchecked") Collection<Object> collection = (Collection<Object>) object;

      NettyByteBufUtil.writeInt32(buf, collection.size());
      NettyByteBufUtil.writeInt32(buf, 0);

      for (Object o : collection) {
        serializer.writeObject(buf, o);
      }
    }
  }


}
