package com.viddu.infinispan.redis.configuration

import java.net.URI

import org.infinispan.commons.configuration.Builder
import org.infinispan.commons.configuration.attributes.AttributeSet
import org.infinispan.configuration.cache.{PersistenceConfigurationBuilder, AbstractStoreConfigurationBuilder}
import redis.clients.jedis.Protocol

/**
 * Created by Viddu on 6/27/2015.
 */
class RedisStoreConfigurationBuilder(builder: PersistenceConfigurationBuilder)
  extends AbstractStoreConfigurationBuilder[RedisStoreConfiguration, RedisStoreConfigurationBuilder](builder) {

  var host: String = null
  var port: Int = 0
  var timeout: Int = 0
  var password: String = null
  var database: Int = 0
  var clientName: String = null
  var dbURL: URI = null

  override def create(): RedisStoreConfiguration = new RedisStoreConfiguration(attributes.protect(), async.create(), singletonStore.create(), this)

  override def self(): RedisStoreConfigurationBuilder = this

  override def read(template: RedisStoreConfiguration): Builder[_] = {
    this.host = template.host
    this.port = template.port
    this.timeout = template.timeout
    this.password = template.password
    this.database = template.database
    this.clientName = template.clientName
    this.dbURL = template.dbURL
    self
  }

  def url(redisURI: URI) = {
    this.dbURL = redisURI
    this.host = redisURI.getHost
    this.port = redisURI.getPort
    this.timeout = Protocol.DEFAULT_TIMEOUT
    this.password = if ((redisURI.getUserInfo != null)) redisURI.getUserInfo.split(":", 2)(1) else null
    this.database = if ((redisURI.getPath != null)) redisURI.getPath.substring(1).toInt else 0
    self
  }

  def host(h: String): RedisStoreConfigurationBuilder = {
    this.host = h
    self
  }

  def port(p: Int): RedisStoreConfigurationBuilder = {
    this.port = p
    self
  }

  def timeout(t: Int): RedisStoreConfigurationBuilder = {
    this.timeout = t
    self
  }

  def password(p: String): RedisStoreConfigurationBuilder = {
    this.password = p
    self
  }

  def database(d: Int): RedisStoreConfigurationBuilder = {
    this.database = d
    self
  }

  def clientName(c: String): RedisStoreConfigurationBuilder = {
    this.clientName = c
    self
  }
}
