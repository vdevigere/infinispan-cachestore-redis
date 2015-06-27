package com.viddu.infinispan.redis

import redis.clients.jedis.{Jedis, JedisPool}

/**
 * Created by Viddu on 6/27/2015.
 */
object JedisConnection {
  def jedisConnection[T](pool: JedisPool)(op: Jedis => T) = {
    val jedis = pool.getResource
    try {
      op(jedis)
    } finally {
      jedis.close
    }
  }
}
