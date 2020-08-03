package com.air.antispider.stream.dataprocess.launch

import com.air.antispider.stream.common.bean.{AccessLog, AnalyzeRule, BookRequestData, QueryRequestData}
import com.air.antispider.stream.common.util.jedis.{JedisConnectionUtil, PropertiesUtil}
import com.air.antispider.stream.dataprocess.businessprocess._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * 业务处理主类
 */

object DataProcessLauncher {


  def main(args: Array[String]): Unit = {
    //如果应用被停止，当前任务执行完再停止(优雅地停止)
    System.setProperty("spark.streaming.stopGracefullyOnShutdown", "true")

    //初始化环境
    val conf = new SparkConf().setAppName("DataProcessLauncher").setMaster("local[2]")
    val sc = new SparkContext(conf)

    //consumer grouping
    val groupid = "group1905"

    //kafka params(参数)
    val kafkaParams = Map[String, Object] (
      "bootstrap.servers" -> PropertiesUtil.getStringByKey("default.brokers", "kafkaConfig.properties"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupid,
      "auto.offset.reset" -> "earliest"  //当offset异常时，从开始消费

    )

    //topic
    val topics = Set(PropertiesUtil.getStringByKey("source.nginx.topic", "kafkaConfig.properties"))

    //创建处理方法
    val ssc = setupSsc(sc, kafkaParams, topics)

    ssc.start()
    ssc.awaitTermination()





  }

  /**
   * 业务处理方法
   * @param sc
   * @param kafkaParams
   * @param topics
   * @return
   */
  def setupSsc(sc: SparkContext,
               kafkaParams: Map[String, Object],
               topics: Set[String]): StreamingContext = {
    //创建实时处理上下文
    val ssc = new StreamingContext(sc , Seconds(5))

    //消费kafka数据
    val stream = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    stream.map(_.value()).foreachRDD(rdd => rdd.foreach(println))

    //读取数据库的过滤规则信息
    val filterRuleList = AnalyzeRuleDB.queryfilterRule()
    //读取数据库分类规则信息
    val ruleMapTemp = AnalyzeRuleDB.queryRuleMap()
    //获取数据解析-查询规则
    val queryRules = AnalyzeRuleDB.queryRule(0)
    //获取数据解析-预定规则
    val bookRules = AnalyzeRuleDB.queryRule(1)
    //查询ip黑名单
    val ipBlackList: ArrayBuffer[String] = AnalyzeRuleDB.queryIpBlackList()

    //将规则信息广播出去(变化时需要重新广播 所以用var修饰)
    //@volatile 注解  做共享变量的优化  保证读取最新数据
    @volatile var filterRuleBroadcast = sc.broadcast(filterRuleList)
    @volatile var ruleMapBroadcast = sc.broadcast(ruleMapTemp)

    //定义一个map将解析规则进行封装
    val queryBooks = new mutable.HashMap[String, List[AnalyzeRule]]()
    queryBooks.put("queryRules", queryRules)
    queryBooks.put("bookRules", bookRules)
    // 广播查询预定规则
    @volatile var queryBookRulesBroadcast = sc.broadcast(queryBooks)
    @volatile var ipBlackListBroadcast = sc.broadcast(ipBlackList)



    //结果需要对接到redis中去，先拿到jedis链接
    val jedis = JedisConnectionUtil.getJedisCluster  //getJedis为单节点redis方法  getJedisCluster为集群

    //数据处理
    stream.map(_.value()).foreachRDD(rdd => {
      //开启RDD缓存，以便重用rdd
      val cachedRDD: RDD[String] = rdd.cache()

      //监控过滤规则是否发生改变
      filterRuleBroadcast = BroadcastProcess.broadFilterRule(sc, filterRuleBroadcast, jedis)
      //监控过滤规则是否发生改变
      ruleMapBroadcast = BroadcastProcess.classiferRule(sc, ruleMapBroadcast, jedis)
      //监控解析规则是否发生改变
      queryBookRulesBroadcast = BroadcastProcess.monitorQueryBooksRule(sc, queryBookRulesBroadcast, jedis)
      //监控数据库黑名单是否发生变化
      ipBlackListBroadcast = BroadcastProcess.monitorBlackListRule(sc, ipBlackListBroadcast, jedis)

      //对数据进行处理
      val value: RDD[AccessLog] = DataSplit.parseAccessLog(rdd)

      //链路统计
      val serverCount = BusinessProcess.linkCount(value, jedis)
      //为了做监控统计生成的各个链路的流量
      val serverCountMap = serverCount.collectAsMap()

      //数据清洗功能
      val filteredRDD = value.filter(log => UrlFilter.filterUrl(log, filterRuleBroadcast))

      //数据处理

      //脱敏
      val dataProcess = filteredRDD.map(log => {
        //对手机号进行脱敏
        log.http_cookie = EncryedData.encryptedPhone(log.http_cookie)

        //对身份证号进行脱敏
        log.http_cookie = EncryedData.encryptedId(log.http_cookie)

        //分类打标签：国内预定、国际预定、国内查询、国际查询
        val requestTypeLable = RequestTypeClassifer.classifyByRequest(
          log.request, ruleMapBroadcast.value
        )
        //是否往返标签
        val travelType = TravelTypeClassifier.classifyByRefererAndRequestBody(log.http_referer)

        //数据解析-查询数据的解析
        val queryRequestData: Option[QueryRequestData] = AnalyzeRequest.analyzeQueryRequest(
          requestTypeLable, log.request_method,
          log.content_type, log.request, log.request_body, travelType,
          queryBookRulesBroadcast.value("queryRules"))
        queryRequestData
        //数据解析-预定数据的解析
        val bookRequestData: Option[BookRequestData] = AnalyzeBookRequest.analyzeBookRequest(
          requestTypeLable, log.request_method,
          log.content_type, log.request, log.request_body, travelType,
          queryBookRulesBroadcast.value("queryRules"))
        bookRequestData
        //判断高频ip,生成对应布尔标签
        val highFrqIPGroup = IpOperation.isFreIP(log.remote_addr, ipBlackListBroadcast.value)
        //数据结构化
        DataPackage.dataPackage("", log, highFrqIPGroup, requestTypeLable,
          travelType, queryRequestData, bookRequestData)

      })

      /**
       * 数据推送
       */

      //查询行为数据
      DataSend.sendQueryData2Kafka(dataProcess)

      //预定行为数据
      DataSend.sendBookData2Kafka(dataProcess)

      //spark实时监控系统流量
      SparkStreamingMonitor.streamMonitor(sc, rdd, serverCountMap, jedis)

      //释放资源
      rdd.unpersist()

    })



    ssc
  }


}
