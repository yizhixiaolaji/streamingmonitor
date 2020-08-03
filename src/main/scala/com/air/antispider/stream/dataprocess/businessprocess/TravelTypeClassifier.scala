package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.dataprocess.constants.TravelTypeEnum

/**
 * 是否为往返标签
 */
object TravelTypeClassifier {
  /**
   * 往返   单程
   * @param http_referer
   * @return
   */
  def classifyByRefererAndRequestBody(http_referer: String) = {
    //定义一个日期的个数
    var dateCounts = 0
    //定义日期匹配的正则
    val regex = "^(\\d{4})-(0\\d{1}|1[0-2])-(0\\d{1}|[12]\\d{1}|3[01])$"

    //判断当前日期URL是否携带参数
    if (http_referer.contains("?") && http_referer.split("\\?").length > 1) {
      //切分数据
      val params = http_referer.split("\\?")(1).split("&")
      //遍历数据的值
      params.foreach(p => {
        //按照 ”= “进行切分
        val kv = p.split("=")
        //匹配正则
        if (kv(1).matches(regex)) {
          //累加
          dateCounts = dateCounts + 1
        }
      })
    }
    //返回标签
    if (dateCounts == 1) {
      //单程
      TravelTypeEnum.OneWay
    } else if (dateCounts == 2) {
      //往返
      TravelTypeEnum.RoundTrip
    } else {
      //其他
      TravelTypeEnum.Unknown
    }

  }

}
