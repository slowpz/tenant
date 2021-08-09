package org.example.handler.facde;

import org.example.handler.Packet;

/**
 * 世界你好，门面
 *
 * @author ZJP
 * @since 2021年07月22日 21:58:02
 **/
public class HelloWorldFacade {

  /** 回声协议 */
  public static final int ECHO_REQ = 1;


  /**
   * 回声
   *
   * @param str 内容
   * @since 2021年07月22日 21:58:45
   */
  @Packet(ECHO_REQ)
  public String echo(String str) {
    return str;
  }

}