package com.air.antispider.stream.dataprocess.businessprocess

import com.air.antispider.stream.common.bean.AccessLog
import org.apache.spark.broadcast.Broadcast

import scala.collection.mutable.ArrayBuffer

object UrlFilter {
  /**
   * 数据清洗方法，清洗掉静态文件和对应的垃圾数据
   * @param log
   * @param filterRuleBroadcast
   * @return
   */
  def filterUrl(log: AccessLog,
                filterRuleBroadcast: Broadcast[ArrayBuffer[String]]): Boolean = {

    //设置标识，用于用户匹配数据
    var isMatch = true

    filterRuleBroadcast.value.foreach(str => {
      if (log.request.matches(str)){
        isMatch = false
      }
    })

    isMatch



  }

}
