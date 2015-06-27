package com.viddu.infinispan.redis.configuration

import com.viddu.infinispan.redis.RedisCacheLoaderWriter
import org.infinispan.commons.configuration.{ConfigurationFor, BuiltBy}
import org.infinispan.commons.configuration.attributes.AttributeSet
import org.infinispan.configuration.cache.{AsyncStoreConfiguration, SingletonStoreConfiguration, AbstractStoreConfiguration}

/**
 * Created by Viddu on 6/27/2015.
 */
@ConfigurationFor(classOf[RedisCacheLoaderWriter[_, _]])
@BuiltBy(classOf[RedisStoreConfigurationBuilder])
class RedisStoreConfiguration(attributes: AttributeSet, async: AsyncStoreConfiguration, singletonStore: SingletonStoreConfiguration, builder: RedisStoreConfigurationBuilder) extends AbstractStoreConfiguration(attributes, async, singletonStore){
  val host = builder.host
  val port = builder.port
  val timeout = builder.timeout
  val password = builder.password
  val database = builder.database
  val clientName = builder.clientName
  val dbURL = builder.dbURL
}
