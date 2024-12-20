package org.example.game;


import org.example.common.model.GameId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


/**
 * 子容器配置类
 *
 * @author ZJP
 * @since 2021年06月30日 18:09:27
 **/
@ComponentScan({"org.example.game", "org.example.common"})
@PropertySource("classpath:game.properties")
@Configuration
public class GameConfig {

  @Value("${game.id}")
  private GameId id;
  @Value("${game.port}")
  private int port;
  @Value("${game.idelSec:60}")
  public int idleSec = 60;

  public GameId getId() {
    return id;
  }

  public int getPort() {
    return port;
  }

  public int getIdleSec() {
    return idleSec;
  }
}
