package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.{AccessLog, BookRequestData, CoreRequestParams, ProcessedData, QueryRequestData, RequestType}
import com.air.antispider.stream.common.util.decode.MD5
import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum

object DataPackage {
  /**
   * 封装结构化数据
   * @param str
   * @param log
   * @param highFrqIPGroup
   * @param requestTypeLable
   * @param travelType
   * @param queryRequestData
   * @param bookRequestData
   */
  def dataPackage(str: String, log: AccessLog, highFrqIPGroup: Boolean,
                  requestTypeLable: RequestType,
                  travelType: TravelTypeEnum.Value,
                  queryRequestData: Option[QueryRequestData],
                  bookRequestData: Option[BookRequestData]) = {
    /**
     * 注意： 在求飞行时间、始发地、目的地的时候，一定要将预定放到查询之前，因为预定包含查询
     *
     */

    //飞行时间
    var flightDate = ""
    bookRequestData match {
      case Some(book) => flightDate = book.flightDate.mkString
      case None => println("null")
    }
    queryRequestData match {
      case Some(query) => flightDate = query.flightDate
      case None => println("null")
    }

    //始发地
    var depCity = ""
    bookRequestData match {
      case Some(book) => depCity = book.depCity.mkString
      case None => println("null")
    }
    queryRequestData match {
      case Some(query) => depCity = query.depCity
      case None => println("null")
    }

    //目的地
    var arrCity = ""
    bookRequestData match {
      case Some(book) => arrCity = book.arrCity.mkString
      case None => println("null")
    }
    queryRequestData match {
      case Some(query) => arrCity = query.arrCity
      case None => println("null")
    }

    //封装飞行时间，始发地，目的地
    val params = CoreRequestParams(flightDate, depCity, arrCity)

    //对log.jessionID, log.userID做脱敏
    val md5 = new MD5
    val jessionID = md5.getMD5ofStr(log.jessionID)
    val userID = md5.getMD5ofStr(log.userID)

    // 封装主要参数并返回

    ProcessedData(str, log.request_method, log.request, log.remote_addr,
      log.http_user_agent, log.time_iso8601, log.server_addr,
      highFrqIPGroup, requestTypeLable, travelType, params, jessionID, userID,
      queryRequestData, bookRequestData, log.http_referer)

  }

}
