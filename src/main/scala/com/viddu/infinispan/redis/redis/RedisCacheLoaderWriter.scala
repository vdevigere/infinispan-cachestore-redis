package com.viddu.infinispan.redis

import java.util
import java.util.concurrent.{Callable, Executor}

import com.viddu.infinispan.redis.configuration.RedisStoreConfiguration
import org.infinispan.commons.marshall.StreamingMarshaller
import org.infinispan.executors.ExecutorAllCompletionService
import org.infinispan.filter.KeyFilter
import org.infinispan.marshall.core.MarshalledEntry
import org.infinispan.persistence.TaskContextImpl
import org.infinispan.persistence.spi.AdvancedCacheLoader.CacheLoaderTask
import org.infinispan.persistence.spi.AdvancedCacheWriter.PurgeListener
import org.infinispan.persistence.spi.{PersistenceException, InitializationContext, AdvancedLoadWriteStore}
import redis.clients.jedis.{JedisPoolConfig, JedisPool}
import JedisConnection._
import collection.JavaConversions._
import scala.collection.mutable.Set

/**
 * Created by Viddu on 6/27/2015.
 */
class RedisCacheLoaderWriter[K, V] extends AdvancedLoadWriteStore[K, V] {
  var marshaller: StreamingMarshaller = null

  var configuration: RedisStoreConfiguration = null

  var context: InitializationContext = null

  var pool: JedisPool = null

  override def process(filter: KeyFilter[_ >: K], task: CacheLoaderTask[K, V], executor: Executor, fetchValue: Boolean, fetchMetadata: Boolean): Unit = {
    jedisConnection(pool) {
      jedis => {
        val keyBytes: Array[Byte] = "*".getBytes()
        val keys: Set[Array[Byte]] = jedis.keys(keyBytes)
        val eacs: ExecutorAllCompletionService = new ExecutorAllCompletionService(executor)
        val taskContext: TaskContextImpl = new TaskContextImpl
        for (key <- keys) {
          val marshalledKey: K = marshaller.objectFromByteBuffer(key).asInstanceOf[K]
          if (filter == null || filter.accept(marshalledKey)) {
            if (!taskContext.isStopped) {
              eacs.submit(new Callable[Void] {
                override def call(): Void = {
                  val marshalledEntry: MarshalledEntry[K, V] = load(marshalledKey)
                  if (marshalledEntry != null) {
                    task.processEntry(marshalledEntry, taskContext)
                  }
                  return null
                }
              })
            }
          }
        }
        eacs.waitUntilAllCompleted
        if (eacs.isExceptionThrown) {
          throw new PersistenceException("Execution exception!", eacs.getFirstException)
        }
      }
    }
  }

  override def size(): Int = {
    val size = jedisConnection[Long](pool) {
      jedis => jedis.dbSize()
    }
    size.toInt
  }

  override def clear(): Unit = {
    jedisConnection(pool) {
      jedis => jedis.flushDB()
    }
  }

  override def purge(threadPool: Executor, listener: PurgeListener[_ >: K]): Unit = {}

  override def init(ctx: InitializationContext): Unit = {
    this.context = ctx
    this.configuration = ctx.getConfiguration.asInstanceOf[RedisStoreConfiguration]
    this.marshaller = this.context.getMarshaller
    this.pool = new JedisPool(new JedisPoolConfig, configuration.host,
      configuration.port, configuration.timeout, configuration.password)
  }

  override def delete(key: scala.Any): Boolean = {
    jedisConnection[Boolean](pool) {
      jedis => jedis.del(marshaller.objectToByteBuffer(key)) > 0
    }
  }

  override def write(entry: MarshalledEntry[_ <: K, _ <: V]): Unit = {
    jedisConnection(pool) {
      jedis => jedis.set(marshaller.objectToByteBuffer(entry.getKey), marshaller.objectToByteBuffer(entry))
    }
  }

  override def load(key: scala.Any): MarshalledEntry[K, V] = {
    jedisConnection[MarshalledEntry[K, V]](pool) {
      jedis => {
        val entry = jedis.get(marshaller.objectToByteBuffer(key))
        if (entry != null)
          marshaller.objectFromByteBuffer(entry).asInstanceOf[MarshalledEntry[K, V]]
        else
          null
      }
    }
  }

  override def contains(key: scala.Any): Boolean = {
    jedisConnection[Boolean](pool) {
      jedis => jedis.exists(marshaller.objectToByteBuffer(key))
    }
  }

  override def stop(): Unit = {}

  override def start(): Unit = {
    if (configuration.purgeOnStartup()) {
      jedisConnection(pool) {
        jedis => jedis.flushDB()
      }
    }
  }
}
