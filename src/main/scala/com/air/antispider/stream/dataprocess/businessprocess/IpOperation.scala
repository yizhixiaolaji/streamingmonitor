package com.air.antispider.stream.dataprocess.businessprocess

import scala.collection.mutable.ArrayBuffer

object IpOperation {
  /**
   * 匹配高频ip
   * @param remote_addr
   * @param value
   */
  def isFreIP(remote_addr: String, value: ArrayBuffer[String]) = {
    //初始化标记
    var flag = false
    //从广播变量获取黑名单进行匹配
    val it = value.iterator
    while (it.hasNext) {
      val str = it.next()
      //匹配
      if (str.eq(remote_addr)){
        flag = true
      }
    }
    flag
  }

}
