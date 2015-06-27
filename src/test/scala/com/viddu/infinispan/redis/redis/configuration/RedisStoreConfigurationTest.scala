package com.viddu.infinispan.redis.configuration

import org.assertj.core.api.Assertions._
import org.infinispan.configuration.cache.{ConfigurationBuilder, Configuration}
import org.junit.Test

/**
 * Created by Viddu on 6/27/2015.
 */

class RedisStoreConfigurationTest {

  @Test
  def testCacheStoreConfiguration {
    val configuration: Configuration = new ConfigurationBuilder().persistence.addStore(classOf[RedisStoreConfigurationBuilder])
      .database(0).clientName("CLIENT").host("localhost").password("password").port(8080).timeout(30).build
    val conf: RedisStoreConfiguration = configuration.persistence.stores.get(0).asInstanceOf[RedisStoreConfiguration]
    assertThat(conf.database).isEqualTo(0)
    assertThat(conf.clientName).isEqualTo("CLIENT")
    assertThat(conf.host).isEqualTo("localhost")
    assertThat(conf.password).isEqualTo("password")
    assertThat(conf.port).isEqualTo(8080)
    assertThat(conf.timeout).isEqualTo(30)
  }
}
