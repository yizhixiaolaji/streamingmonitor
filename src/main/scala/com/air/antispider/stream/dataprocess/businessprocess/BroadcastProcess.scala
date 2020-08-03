package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.AnalyzeRule
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import redis.clients.jedis.JedisCluster

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * 更新广播变量
 */
object BroadcastProcess {


  /**
   * 如果过滤规则改变，则更新过滤规则
   *
   * @param sc
   * @param filterRuleBroadcast
   * @param jedis
   * @return
   */
  def broadFilterRule(sc: SparkContext,
                      filterRuleBroadcast: Broadcast[ArrayBuffer[String]],
                      jedis: JedisCluster): Broadcast[ArrayBuffer[String]] = {
    //查询redis中的标识
    val needUpdateFileList = jedis.get("FilterChangeFlag")

    //判断标识是否为空，或是否发生改变
    if (needUpdateFileList != null &&
      !needUpdateFileList.isEmpty &&
      needUpdateFileList.toBoolean) {
      //查询数据库规则
      val filterRuleListUpdate = AnalyzeRuleDB.queryfilterRule()
      //删除之前广播变量的值
      filterRuleBroadcast.unpersist()
      //重新设置标识
      jedis.set("FilterChangeFlag", "false")
      //重新广播
      sc.broadcast(filterRuleListUpdate)

    } else {
      //如果没有更新就返回当前广播变量
      filterRuleBroadcast
    }

  }

  /**
   * 更新分类规则
   *
   * @param sc
   * @param ruleMapBroadcast
   * @param jedis
   * @return
   */
  def classiferRule(sc: SparkContext,
                    ruleMapBroadcast: Broadcast[mutable.HashMap[String, ArrayBuffer[String]]],
                    jedis: JedisCluster): Broadcast[mutable.HashMap[String, ArrayBuffer[String]]] = {
    //请求分类规则变更标识
    val needUpdateClassifyRule = jedis.get("ClassifyRuleChangeFlag")

    //判断mysql里的规则是否改变
    if (!needUpdateClassifyRule.isEmpty && needUpdateClassifyRule.toBoolean) {
      //查询规则信息
      val ruleMapTemp = AnalyzeRuleDB.queryRuleMap()
      //释放广播变量
      ruleMapBroadcast.unpersist()
      //设置标识
      jedis.set("ClassifyRuleChangeFlag", "false")

      //重新广播
      sc.broadcast(ruleMapTemp)

    }

    ruleMapBroadcast
  }

  /**
   * 解析规则更新
   *
   * @param sc
   * @param queryBookRulesBroadcast
   * @param jedis
   * @return
   */
  def monitorQueryBooksRule(sc: SparkContext,
                            queryBookRulesBroadcast: Broadcast[mutable.HashMap[String, List[AnalyzeRule]]],
                            jedis: JedisCluster): Broadcast[mutable.HashMap[String, List[AnalyzeRule]]] = {
    // 规则变更标识
    val needUpdateAnalyzeRule: String = jedis.get("AnalyzeRuleChangeFlag")
    if (!needUpdateAnalyzeRule.isEmpty && needUpdateAnalyzeRule.toBoolean) {
      //查询规则
      val queryRules = AnalyzeRuleDB.queryRule(0)
      //预定规则
      val bookRules = AnalyzeRuleDB.queryRule(1)
      // 定义map存储数据
      val queryBooks = new mutable.HashMap[String, List[AnalyzeRule]]()
      queryBooks.put("queryRules", queryRules)
      queryBooks.put("bookRules", bookRules)

      queryBookRulesBroadcast.unpersist()
      jedis.set("AnalyzeRuleChangeFlag", "false")
      sc.broadcast(queryRules)
    }

    queryBookRulesBroadcast

  }

  /**
   * 更新黑名单
   *
   * @param sc
   * @param ipBlackListBroadcast
   * @param jedis
   * @return
   */
  def monitorBlackListRule(sc: SparkContext,
                           ipBlackListBroadcast: Broadcast[ArrayBuffer[String]],
                           jedis: JedisCluster): Broadcast[ArrayBuffer[String]] = {

    // 黑名单变更标识
    val needUpdateIpBlackRule = jedis.get("IpBlackRuleChangeFlag")
    if (!needUpdateIpBlackRule.isEmpty && needUpdateIpBlackRule.toBoolean) {
      // 查询黑名单
      val ipBlackList = AnalyzeRuleDB.queryIpBlackList()

      ipBlackListBroadcast.unpersist()
      jedis.set("IpBlackRuleChangeFlag", "false")
      sc.broadcast(ipBlackList)


    }
    ipBlackListBroadcast

  }
}