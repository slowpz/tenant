package org.example.world;


import org.example.common.model.ReqMove;
import org.example.net.anno.Req;
import org.example.net.anno.Rpc;
import org.example.net.handler.RawExecutorSupplier;

/**
 * 世界门面(文档生产插件测试)
 *
 * @author ZJP
 * @since 2021年09月27日 15:04:09
 **/
@Rpc
public class WorldFacade implements RawExecutorSupplier {

  /**
   * 你说什么我就说什么
   *
   * @param str 内容
   * @author ZJP
   * @since 2021年09月27日 15:15:19
   **/
  @Req(100)
  public String echo(String str) {
    return str;
  }

  /**
   * 移动请求
   *
   * @param move 移动数据
   * @since 2021年09月27日 15:33:00
   */
  @Req(101)
  public void move(ReqMove move) {
    System.out.println(move);
  }
}
