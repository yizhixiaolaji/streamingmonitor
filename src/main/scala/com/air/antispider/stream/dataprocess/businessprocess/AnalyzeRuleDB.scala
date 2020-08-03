package com.air.antispider.stream.dataprocess.businessprocess

import java.sql.{Connection, PreparedStatement, ResultSet}

import com.air.antispider.stream.common.util.database.{C3p0Util, QueryDB}
import com.air.antispider.stream.dataprocess.constants.{BehaviorTypeEnum, FlightTypeEnum}
import com.air.antispider.stream.common.bean.AnalyzeRule

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * 获取数据库规则
 */
object AnalyzeRuleDB {



  /**
   * 查询过滤规则
   */
  def queryfilterRule() = {
  //SQL
    val sql = "select value from nh_filter_rule"
    //指定要查询的字段
    val field = "value"
    //加载sql进行查询
    val valueArr = QueryDB.queryData(sql,field)

    valueArr
  }

  /**
   * 查询数据分类规则
   */
  def queryRuleMap() = {
    //从数据库中查找航班分类规则-国内航班
    val nqsql = "select expression from nh_classify_rule where flight_type = " +
    FlightTypeEnum.National.id + " and operation_type = " +
    BehaviorTypeEnum.Query.id

    //从数据库中查找航班分类规则-国际查询
    val iqsql = "select expression from nh_classify_rule where flight_type = " +
      FlightTypeEnum.International.id + " and operation_type = " +
      BehaviorTypeEnum.Query.id

    //从数据库中查找航班分类规则-国内预定
    val nbsql = "select expression from nh_classify_rule where flight_type = " +
      FlightTypeEnum.National.id + " and operation_type = " +
      BehaviorTypeEnum.Book.id

    //从数据库中查找航班分类规则-国际预定
    val ibsql = "select expression from nh_classify_rule where flight_type = " +
      FlightTypeEnum.International.id + " and operation_type = " +
      BehaviorTypeEnum.Book.id

    //指定查询字段
    val expression = "expression"

    //开始查询
    val nationalQueryArr = QueryDB.queryData(nqsql,expression)
    val internationalQueryArr = QueryDB.queryData(iqsql,expression)
    val nationalBookArr = QueryDB.queryData(nbsql,expression)
    val internationalBookArr = QueryDB.queryData(ibsql,expression)

    //封装数据到MAP
    val ruleMapTemp: mutable.HashMap[String, ArrayBuffer[String]] =
      new mutable.HashMap[String, ArrayBuffer[String]]()
    ruleMapTemp.put("nq", nationalQueryArr)
    ruleMapTemp.put("iq", internationalQueryArr)
    ruleMapTemp.put("nb", nationalBookArr)
    ruleMapTemp.put("ib", internationalBookArr)

    ruleMapTemp
  }

  /**
   *获取查询或预定规则
   * @param behaviorType
   */
  def queryRule(behaviorType: Int) = {
    //用于存放查询出的规则信息
    var analyzeRuleList = new ArrayBuffer[AnalyzeRule]()

    val sql = "select * from analyzerule where behavior_type = " + behaviorType

    //获取链接  查询数据
    var conn: Connection = null
    var ps: PreparedStatement = null
    var rs: ResultSet = null
    try {
      conn = C3p0Util.getConnection
      ps = conn.prepareStatement(sql)
      rs = ps.executeQuery()
      while (rs.next()) {
        val analyzeRule = new AnalyzeRule()
        analyzeRule.id = rs.getString("id")
        analyzeRule.flightType = rs.getString("flight_type").toInt
        analyzeRule.BehaviorType = rs.getString("behavior_type").toInt
        analyzeRule.requestMatchExpression = rs.getString("requestMatchExpression")
        analyzeRule.requestMethod = rs.getString("requestMethod")
        analyzeRule.isNormalGet = rs.getString("isNormalGet").toBoolean
        analyzeRule.isNormalForm = rs.getString("isNormalForm").toBoolean
        analyzeRule.isApplicationJson = rs.getString("isApplicationJson").toBoolean
        analyzeRule.isTextXml = rs.getString("isTextXml").toBoolean
        analyzeRule.isJson = rs.getString("isJson").toBoolean
        analyzeRule.isXML = rs.getString("isXML").toBoolean
        analyzeRule.formDataField = rs.getString("formDataField")
        analyzeRule.book_bookUserId = rs.getString("book_bookUserId")
        analyzeRule.book_bookUnUserId = rs.getString("book_bookUnUserId")
        analyzeRule.book_psgName = rs.getString("book_psgName")
        analyzeRule.book_psgType = rs.getString("book_psgType")
        analyzeRule.book_idType = rs.getString("book_idType")
        analyzeRule.book_idCard = rs.getString("book_idCard")
        analyzeRule.book_contractName = rs.getString("book_contractName")
        analyzeRule.book_contractPhone = rs.getString("book_contractPhone")
        analyzeRule.book_depCity = rs.getString("book_depCity")
        analyzeRule.book_arrCity = rs.getString("book_arrCity")
        analyzeRule.book_flightDate = rs.getString("book_flightDate")
        analyzeRule.book_cabin = rs.getString("book_cabin")
        analyzeRule.book_flightNo = rs.getString("book_flightNo")
        analyzeRule.query_depCity = rs.getString("query_depCity")
        analyzeRule.query_arrCity = rs.getString("query_arrCity")
        analyzeRule.query_flightDate = rs.getString("query_flightDate")
        analyzeRule.query_adultNum = rs.getString("query_adultNum")
        analyzeRule.query_childNum = rs.getString("query_childNum")
        analyzeRule.query_infantNum = rs.getString("query_infantNum")
        analyzeRule.query_country = rs.getString("query_country")
        analyzeRule.query_travelType = rs.getString("query_travelType")
        analyzeRule.book_psgFirName = rs.getString("book_psgFirName")
        analyzeRuleList += analyzeRule
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    // 返回值
    analyzeRuleList.toList


  }

  /**
   * 查询黑名单
   */
  def queryIpBlackList() = {
    val sql = "select ip_name from nh_ip_blacklist"
    val field = "ip_name" //指定查询的字段
    val ipList = QueryDB.queryData(sql, field)
    ipList
  }


}
