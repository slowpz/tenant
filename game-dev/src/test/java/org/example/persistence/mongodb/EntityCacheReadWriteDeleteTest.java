package org.example.persistence.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import org.example.common.persistence.EntityCache;
import org.example.common.persistence.accessor.Accessor;
import org.example.common.persistence.mongodb.MongoDbAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.example.persistence.mongodb.AvatarId.avatarId;

/**
 * 手动配置环境，用Ideal测试吧
 */
@EnabledIfSystemProperty(named = "mongoDbTest", matches = "true")
public class EntityCacheReadWriteDeleteTest {

  /* The mongo client */
  private static long expireTime = 100;
  private static MongoClient client;
  private static EntityCache<Long, Avatar> avatarCache;

  @BeforeAll
  static void beforeAll() {
    client = MongoClients.create(
        "mongodb://game:123456@localhost:27017/test?authSource=admin&connecttimeoutms=30000&sockettimeoutms=300000&appName=appName&maxPoolSize=20&minPoolSize=1");
    Accessor accessor = new MongoDbAccessor(new MongoTemplate(client, "test"));

    MongoClinetConfiguration configuration = new MongoClinetConfiguration();
    avatarCache = new EntityCache<>(expireTime, Avatar.class, accessor, ForkJoinPool.commonPool());
  }

  @AfterAll
  static void afterAll() {
    if (client != null) {
      //清空测试数据
      MongoDatabase database = client.getDatabase("test");
      database.getCollection("avatar").drop();
      client.close();
    }
  }

  @Test
  void multiReadWriteTest() throws Exception {
    String namePrefix = "Test";
    for (long i = 10; i < 20; i++) {
      avatarCache.getOrCreate(i, id -> {
        Avatar a = new Avatar(avatarId(id));
        a.name(namePrefix + id);
        return a;
      });
    }

    TimeUnit.MILLISECONDS.sleep(expireTime * 10);
    //重新读进内存
    for (long i = 10; i < 20; i++) {
      Avatar expected = new Avatar(avatarId(i));
      expected.name(namePrefix + i);
      Assertions.assertEquals(expected, avatarCache.get(i));
    }

    // 删除15-20
    for (long i = 15; i < 20; i++) {
      Avatar expected = new Avatar(avatarId(i));
      expected.name(namePrefix + i);
      Assertions.assertEquals(expected, avatarCache.delete(i));
    }

    TimeUnit.MILLISECONDS.sleep(expireTime * 10);
    //看下10-14是否还在
    for (long i = 10; i < 15; i++) {
      Avatar expected = new Avatar(avatarId(i));
      expected.name(namePrefix + i);
      Assertions.assertEquals(expected, avatarCache.get(i));
    }

    //看下15-20是否删除了
    for (long i = 15; i < 20; i++) {
      Assertions.assertNull(avatarCache.get(i));
    }

  }

}
