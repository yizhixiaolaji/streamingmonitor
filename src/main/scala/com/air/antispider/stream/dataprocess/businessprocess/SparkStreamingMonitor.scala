package com.air.antispider.stream.dataprocess.businessprocess

import java.text.SimpleDateFormat
import java.util.Date

import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.common.util.spark.SparkMetricsUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.JedisCluster

import scala.collection.mutable

object SparkStreamingMonitor {
  /**
   * 实时监控数据流量
   * @param sc
   * @param rdd
   * @param serverCountMap
   * @param jedis
   */

  def streamMonitor(sc: SparkContext,
                    rdd: RDD[String],
                    serverCountMap: collection.Map[String, Int],
                    jedis: JedisCluster) = {
    //获取apppid
    val appid = sc.applicationId
    //appname
    val appname = sc.appName
    //当前模式需要监控的4040服务路径
    val url =  "http://localhost:4040/metrics/json/"

    //获取4040服务下的json数据
    val jsonOjb = SparkMetricsUtils.getMetricsJson(url)
    //获取gauges数据
    val gaugesLog = jsonOjb.getJSONObject("gauges")

    //获取
    val startTimePath = appid + ".dirver."+ appname +".StreamingMetrics.streaming.lastCompletedBatch_processingStartTime"
    //开始时间
    val startTime = gaugesLog.getJSONObject(startTimePath)
    //转化为long类型，便于计算
    var processStartTime: Long = 0L
    //判断是否为空
    if (startTime != null) {
      processStartTime = startTime.getLong("value")
    }

    //结束时间
    val endTimePath = appid + ".dirver."+ appname +".StreamingMetrics.streaming.lastCompletedBatch_processingEndTime"
    val endTime = gaugesLog.getJSONObject(endTimePath)
    var processEndTime: Long = 0L
    if (endTime != null) {
      processEndTime = endTime.getLong("value")
    }

    //计算数据行数
    val sourceCount = rdd.count()
    //批次所用时间
    val batchTime = processEndTime - processStartTime
    //平均计算速度
    val countBatchAvg = sourceCount.toDouble / batchTime.toDouble
    //将结束时间转换为时间类型
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val dateEndTime = dateFormat.format(new Date(processEndTime))

    //封装数据到map
    val fieldMap = mutable.Map(
      "endTime" -> dateEndTime,
      "applicationUniqueName" -> appname.toString,
      "applicationId" -> appid.toString,
      "sourceCount" -> sourceCount.toString,
      "costTime" -> batchTime.toString,
      "countPerMillis" -> countBatchAvg.toString,
      "serversCountMap" -> serverCountMap)
    //保存到redis
    try {
      val jedis = JedisConnectionUtil.getJedisCluster
      // 监控记录有效期，单位秒
      val monitorDataExpTime: Int = PropertiesUtil.getStringByKey("cluster.exptime.monitor", "jedisConfig.properties").toInt
      // 产生不重复的key值
      val keyName = PropertiesUtil.getStringByKey("cluster.key.monitor.dataProcess", "jedisConfig.properties") + System.currentTimeMillis.toString
      // 这个key是获取当前批次的最后一条数据的值，因为数据是不断累加的
      val keyNameLast = PropertiesUtil.getStringByKey("cluster.key.monitor.dataProcess", "jedisConfig.properties") + "_LAST"
      // 保存监控数据
      jedis.setex(keyName, monitorDataExpTime, Json(DefaultFormats).write(fieldMap))
      //更新最新的监控数据
      jedis.setex(keyNameLast, monitorDataExpTime, Json(DefaultFormats).write(fieldMap))
      //JedisConnectionUtil.returnRes(jedis)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

  }

}
