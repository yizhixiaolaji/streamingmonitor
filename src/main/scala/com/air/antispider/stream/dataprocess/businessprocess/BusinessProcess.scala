package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.AccessLog
import com.air.antispider.stream.common.util.jedis.PropertiesUtil
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.JedisCluster

import scala.util.Properties

object BusinessProcess {
  /**
   * 用于链路统计
   * @param value
   * @param jedis
   */
  def linkCount(value: RDD[AccessLog], jedis: JedisCluster) = {
    val serverCount: RDD[(String, Int)] = value.map(log => (log.server_addr, 1)).reduceByKey(_+_)

    //活跃连接数
    val activeNum: RDD[(String, Int)] = value.map(log => {
      (log.server_addr, log.connectionActive)
    }).reduceByKey((x, y) => x + y)
    activeNum

    //将数据存到redis（数据存储时要设定生命周期）
    if (!serverCount.isEmpty() && !activeNum.isEmpty()) {
      //调用action，拿到统计链路数据量和活跃连接数
      val serverCountMap: collection.Map[String, Int] = serverCount.collectAsMap()
      val activeNumMap: collection.Map[String, Int] = activeNum.collectAsMap()

      //将结果进行处理
      val map = Map(
        "serverCountMap" -> serverCountMap,
        "activeNumMap" -> activeNumMap
      )
      try {
        //获取链路统计的key(存储数据的key是指定的key+时间戳，这样就可以存储多条数据了)
        val keyName = PropertiesUtil.getStringByKey("cluster.key.monitor.linkProcess","jedisConfig.properties") + System.currentTimeMillis()

        //设置链路统计的过期时间
        val expTime = PropertiesUtil.getStringByKey("cluster.exptime.monitor", "jedisConfig.properties")

        //将数据写入redis，需要设置过期时间
        jedis.setex(keyName, expTime.toInt, Json(DefaultFormats).write(map))
      } catch {
        case e: Exception => {
          e.printStackTrace()
          jedis.close()
        }
      }
    }
    serverCount

  }

}
