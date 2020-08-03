package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.{AccessLog, RequestType}
import com.air.antispider.stream.dataprocess.constants.{BehaviorTypeEnum, FlightTypeEnum}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * 分类打标签
 */
object RequestTypeClassifer {
  /**
   * 根据request URL进行打标签
   * 国内预定（0，1）、国际预定（1，1）、国内查询（0，0）、国际查询（1，0）
   * 其他（-1，-1）
   *
   * @param request
   * @param ruleMap
   * @return
   */
  def classifyByRequest(request: String,
                        ruleMap: mutable.HashMap[String, ArrayBuffer[String]]) = {
    //获取数据库的规则（正则）
    //国内查询
    val nqArr: ArrayBuffer[String] = ruleMap("nq")
    //国际查询
    val iqArr: ArrayBuffer[String] = ruleMap("iq")
    //国内预定
    val nbArr: ArrayBuffer[String] = ruleMap("nb")
    //国际预定
    val ibArr: ArrayBuffer[String] = ruleMap("ib")

    //可变变量
    var flag = true

    //请求参数
    var requestType: RequestType = null

    //国内查询匹配
    nqArr.foreach(rule => {
      //匹配
      if (request.matches(rule)) {
        //匹配上，flag设置为false
        flag = false
        //打标签(0,0)
        requestType = RequestType(FlightTypeEnum.National, BehaviorTypeEnum.Query)

      }
    })

    //国际查询匹配
    iqArr.foreach(rule => {
      //匹配
      if (request.matches(rule)) {
        //匹配上，flag设置为false
        flag = false
        //打标签(1,0)
        requestType = RequestType(FlightTypeEnum.International, BehaviorTypeEnum.Query)

      }
    })

    //国内预定匹配
    nbArr.foreach(rule => {
      //匹配
      if (request.matches(rule)) {
        //匹配上，flag设置为false
        flag = false
        //打标签(0,1)
        requestType = RequestType(FlightTypeEnum.National, BehaviorTypeEnum.Book)

      }
    })

    //国际预定匹配
    ibArr.foreach(rule => {
      //匹配
      if (request.matches(rule)) {
        //匹配上，flag设置为false
        flag = false
        //打标签(1,1)
        requestType = RequestType(FlightTypeEnum.International, BehaviorTypeEnum.Book)
      }
    })

    //如果什么都没有匹配上，返回(-1,-1)
    if (flag){
      requestType = RequestType(FlightTypeEnum.Other, BehaviorTypeEnum.Other)
    }

    requestType
  }




}
