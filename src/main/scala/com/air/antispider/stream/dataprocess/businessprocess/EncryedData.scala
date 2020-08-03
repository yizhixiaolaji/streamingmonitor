package com.air.antispider.stream.dataprocess.businessprocess

import java.util.regex.{Matcher, Pattern}

import com.air.antispider.stream.common.util.decode.MD5

object EncryedData {

//
//  /**
//   * 对手机号进行脱敏
//   *
//   * @param http_cookie
//   * @return
//   */
//  def encryptedPhone(http_cookie: String): String = {
//    //正则：  ((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[0-9])|(18[0,5-9]))\d{8}
//    val md5 = new MD5()
//    //获取http_cookie
//    var cookie = http_cookie
//    //手机号的正则表达式
//    val phonePattern = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[0-9])|(18[0-9]))\\d{8}")
//    //匹配手机号
//    val phoneMatcher: Matcher = phonePattern.matcher(http_cookie)
//    //循环处理多个手机号
//    while (phoneMatcher.find()) {
//      //拿到手机号后，获取手机号前一个字符和后一个字符
//      //拿到的手机号，有可能是有这样的“=”前缀和后“:”缀，需要对数据进行判断处理
//      //手机号前一个index
////      val lowIndex = http_cookie.indexOf(phoneMatcher.group())-1
////      //手机号后一个index
////      val highIndex = lowIndex + phoneMatcher.group().length+1
////      //手机号前一个字符
////      val lowLetter = http_cookie.charAt(lowIndex).toString
////      //匹配当前第一位不是数字
////      if (!lowLetter.matches("^[0-9]$")) {
////        //如果字符串最后时手机号，直接替换
////        if (highIndex < http_cookie.length){
////          //获取手机号最后一个字符
////          val highLetter = http_cookie.charAt(highIndex).toString()
////          //如果后一位也不是数字，说明这个字符串就是一个手机号
////          if (!highLetter.matches("^[0-9]$")) {
////            //直接替换
////           // cookie = cookie.replace(phoneMatcher.group(), md5.getMD5ofStr(phoneMatcher.group()))
////          }
////        }
////      }
//      val lowIndex = http_cookie.indexOf(phoneMatcher.group()) - 1
//      val highIndex = lowIndex + phoneMatcher.group().length() + 1
//      val lowLetter = http_cookie.charAt(lowIndex).toString()
//
//      if (!lowLetter.matches("^[0-9]$")) {
//        if (highIndex < http_cookie.length()) {
//          val highLetter = http_cookie.charAt(highIndex).toString()
//          if (!highLetter.matches("^[0-9]$")) {
//
//            cookie = cookie.replace(phoneMatcher.group(), md5.getMD5ofStr(phoneMatcher.group()))
//
//          }
//
//        } else {
//
//          cookie = cookie.replace(phoneMatcher.group(), md5.getMD5ofStr(phoneMatcher.group()))
//
//        }
//
//        }
//      }
//
//
//
//
//
//
//
//    cookie
//  }
//
//  /**
//   * 对身份证号进行脱敏
//   * @param http_cookie
//   * @return
//   */
//  def encryptedID(http_cookie: String): String = {
//    //身份证号匹配正则： (\d{18})|(\d{17}(\d|X|x))|(\d{15})
//    val md5 = new MD5()
//    var cookie = http_cookie
//    val idPattern = Pattern.compile("(\\d{18})|(\\d{17}(\\d|X|x))|(\\d{15})")
//    //匹配
//    val idMather = idPattern.matcher(http_cookie)
//    while (idMather.find()) {
//      //拿到前一个字符index
//      val lowIndex = http_cookie.indexOf(idMather.group())-1
//      //拿到后一个字符index
//      val highIndex = lowIndex + idMather.group().length+1
//
//      val lowLetter = http_cookie.charAt(lowIndex).toString
//
//      if (!lowLetter.matches("^[0-9]$")) {
//        if (highIndex < http_cookie.length) {
//          val highLetter = http_cookie.charAt(highIndex).toString()
//          if (!highLetter.matches("^[0-9]$")) {
//            cookie = cookie.replace(idMather.group(), md5.getMD5ofStr(idMather.group()))
//
//          }
//        }
//      }
//    }
//
//
//
//
//
//    cookie
//  }
  /**
   * 数据脱敏：手机号
   *
   * @param http_cookie
   * @return
   */
  def encryptedPhone(http_cookie: String): String = {
    // 获取md5加密
    val md5 = new MD5
    // 获取http_cookie
    var cookie = http_cookie
    // 手机号的正则表达式
    val phonePattern = Pattern.compile("((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[0-9])|(18[0-9]))\\d{8}")

    // 匹配手机号
    val phoneMatcher = phonePattern.matcher(http_cookie)

    // 循环处理手机号（多个）
    while (phoneMatcher.find()) {
      // 拿到手机号后，获取手机号的前一个字符和后一个字符
      // 拿到的手机号，有可能是有这样的“=”前缀和“:”后缀组成的这样的数据，需要根据数据进行判断处理
      // 手机号前一个index
      val lowIndex = http_cookie.indexOf(phoneMatcher.group()) - 1
      // 手机号后一个index
      val highIndex = lowIndex + phoneMatcher.group().length + 1
      // 手机号的前一个字符
      val lowLetter = http_cookie.charAt(lowIndex).toString
      // 匹配当前第一位不是数字
      if (!lowLetter.matches("^[0-9]$")) {
        // 如果字符串的最后是手机号，直接替换
        if (highIndex < http_cookie.length) {
          // 获取手机号的最后一个字符
          val highLetter = http_cookie.charAt(highIndex).toString
          // 如果后一位也不是数字，说明这个字符串就是一个手机号
          if (!highLetter.matches("^[0-9]$")) {
            // 直接替换
            cookie = cookie.replace(phoneMatcher.group(), md5.getMD5ofStr(phoneMatcher.group()))
            val a = phoneMatcher.group()
            val b = md5.getMD5ofStr(phoneMatcher.group())
          }
        }
      }
    }

    cookie
  }

  /**
   * 数据脱敏 身份证号
   * @param http_cookie
   * @return
   */
  def encryptedId(http_cookie: String): String = {
    val md5 = new MD5
    var cookie = http_cookie
    val idPattern = Pattern.compile("(\\d{18})|(\\d{17}(\\d|X|x))|(\\d{15})")
    // 匹配
    val idMatcher = idPattern.matcher(http_cookie)
    while (idMatcher.find()) {
      val lowIndex = http_cookie.indexOf(idMatcher.group()) - 1
      val highIndex = lowIndex + idMatcher.group().length + 1
      val lowLetter = http_cookie.charAt(lowIndex).toString
      if (!lowLetter.matches("^[0-9]$")) {
        if (highIndex < http_cookie.length) {
          val highLetter = http_cookie.charAt(highIndex).toString
          if (!highLetter.matches("^[0-9]$")) {
            cookie = cookie.replace(idMatcher.group(), md5.getMD5ofStr(idMatcher.group()))
            val c = idMatcher.group()
            val d = md5.getMD5ofStr(idMatcher.group())

          }
        }
      }

    }

    cookie
  }
}
