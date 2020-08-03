package com.air.antispider.stream.dataprocess.businessprocess

import java.util.Properties

import com.air.antispider.stream.common.bean.ProcessedData
import com.air.antispider.stream.common.util.jedis.PropertiesUtil
import com.air.antispider.stream.dataprocess.constants.BehaviorTypeEnum
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.rdd.RDD

import scala.util.Properties

/**
 * 数据推送类
 */
object DataSend {
  /**
   * 推送query数据
   * @param dataProcess
   */
  def sendQueryData2Kafka(dataProcess: RDD[ProcessedData]) = {
    //过滤出query数据
    val queryData2Kafka = dataProcess
      .filter(p =>p.requestType.behaviorType == BehaviorTypeEnum.Query)
      .map(p => p.toKafkaString())

    //如果有查询数据 则推送查询数据到kafka
    if (!queryData2Kafka.isEmpty()) {
      //topic
      val queryTopic = PropertiesUtil.getStringByKey("source.query.topic", "kafkaConfig.properties")
      //请求kafka的参数
      val kafkaProps = new Properties
      //设置brokers
      kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties"))
      //key序列化方法
      kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, PropertiesUtil.getStringByKey("default.key_serializer_class_config", "kafkaConfig.properties"))
      //value序列化方法
      kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PropertiesUtil.getStringByKey("default.value_serializer_class_config", "kafkaConfig.properties"))
      //批量发送设置：32KB作为一批次或10ms作为一批次
      kafkaProps.put(ProducerConfig.BATCH_SIZE_CONFIG, PropertiesUtil.getStringByKey("default.batch_size_config", "kafkaConfig.properties"))
      kafkaProps.put(ProducerConfig.LINGER_MS_CONFIG, PropertiesUtil.getStringByKey("default.linger_ms_config", "kafkaConfig.properties"))

      //按照分区发送数据
      queryData2Kafka.foreachPartition(it => {
        //创建producer实例
        val producer = new KafkaProducer[String, String](kafkaProps)
        it.foreach(x => {
          //发送数据
          val record = new ProducerRecord[String, String](queryTopic, "null", x)
          producer.send(record)
        })
        producer.close()
      })
    }
  }

  /**
   * 推送book数据
   * @param dataProcess
   */
  def sendBookData2Kafka(dataProcess: RDD[ProcessedData]): Unit = {
    //过滤出query数据
    val bookData2Kafka = dataProcess
      .filter(p =>p.requestType.behaviorType == BehaviorTypeEnum.Book)
      .map(p => p.toKafkaString())

    //如果有查询数据 则推送查询数据到kafka
    if (!bookData2Kafka.isEmpty()) {
      //topic
      val queryTopic = PropertiesUtil.getStringByKey("source.book.topic", "kafkaConfig.properties")
      //请求kafka的参数
      val kafkaProps = new Properties
      //设置brokers
      kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties"))
      //key序列化方法
      kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, PropertiesUtil.getStringByKey("default.key_serializer_class_config", "kafkaConfig.properties"))
      //value序列化方法
      kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PropertiesUtil.getStringByKey("default.value_serializer_class_config", "kafkaConfig.properties"))
      //批量发送设置：32KB作为一批次或10ms作为一批次
      kafkaProps.put(ProducerConfig.BATCH_SIZE_CONFIG, PropertiesUtil.getStringByKey("default.batch_size_config", "kafkaConfig.properties"))
      kafkaProps.put(ProducerConfig.LINGER_MS_CONFIG, PropertiesUtil.getStringByKey("default.linger_ms_config", "kafkaConfig.properties"))

      //按照分区发送数据
      bookData2Kafka.foreachPartition(it => {
        //创建producer实例
        val producer = new KafkaProducer[String, String](kafkaProps)
        it.foreach(x => {
          //发送数据
          val record = new ProducerRecord[String, String](queryTopic, "null", x)
          producer.send(record)
        })
        producer.close()
      })

    }
  }

}
