package org.example.net;

import io.netty.channel.Channel;
import org.example.handler.Handler;
import org.example.handler.HandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息分发调度者(服务器之间)
 *
 * @author ZJP
 * @since 2021年07月24日 14:41:27
 **/
public class CrossDispatcher {

  /** 消息处理器集合 */
  private HandlerRegistry handlerRegistry;
  /** 日志 */
  private Logger logger;

  public CrossDispatcher(HandlerRegistry handlerRegistry) {
    this.handlerRegistry = handlerRegistry;
    logger = LoggerFactory.getLogger(getClass());
  }

  /**
   * 因为Logger隔离有点困难，但为了以后方便，Logger还是先有外部传进来
   *
   * @since 2021年07月24日 15:53:52
   */
  public CrossDispatcher(Logger logger, HandlerRegistry handlerRegistry) {
    this.logger = logger;
    this.handlerRegistry = handlerRegistry;
  }

  /**
   * 根据{@link Message#proto()}进行消息分发
   *
   * @param channel 通信channel
   * @param req 请求消息
   * @since 2021年07月24日 15:58:39
   */
  public void doDispatcher(Channel channel, Message req) {
    Handler handler = handlerRegistry.getHandler(req.proto());
    if (handler == null && req.msgId() == 0) {
      logger.error("地址:{}, 协议号:{}, 消息ID:{} 无对应处理器", channel.remoteAddress(), req.proto(),
          req.msgId());
      return;
    }

    if (handler == null) {
      invokeFuture(channel, req);
    } else {
      invokeHandler(channel, req, handler);
    }
  }

  /**
   * 执行回调
   *
   * @param msg 请求消息
   * @since 2021年08月15日 20:22:04
   */
  private void invokeFuture(Channel channel, Message msg) {
    Connection connection = channel.attr(Connection.CONNECTION).get();
    if (connection == null) {
      return;
    }

    InvokeFuture future = connection.removeInvokeFuture(msg.msgId());
    if (future != null) {
      future.putResult(msg);
      future.cancelTimeout();
      try {
        future.executeCallBack();
      } catch (Exception e) {
        logger.error("Exception caught when executing invoke callback, id={}",
            msg.msgId(), e);
      }
    } else {
      logger
          .warn("Cannot find InvokeFuture, maybe already timeout, id={}, from={} ",
              msg.msgId(),
              channel.remoteAddress());
    }
  }

  /**
   * 执行处理器
   *
   * @param msg 请求消息
   * @param handler 注册的处理器
   * @since 2021年08月15日 20:22:04
   */
  private void invokeHandler(Channel channel, Message msg, Handler handler) {
    try {
      Object result = handler.invoke(msg.packet());
      //
      if (result != null && 0 < msg.proto()) {
        Message response = Message
            .of(Math.negateExact(msg.proto()))
            .msgId(msg.msgId())
            .packet(result);
        channel.write(response);
      }
    } catch (Exception e) {
      channel.write(
          Message
              .of(Math.negateExact(msg.proto()))
              .msgId(msg.msgId())
              .status(MessageStatus.SERVER_EXCEPTION)
      );
      logger.error("from:{}, proto:{}, handler error", channel.remoteAddress(), msg.proto(), e);
    }
  }

}