package com.meetup.iap

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.joda.time.DateTime
import java.text.SimpleDateFormat

class AppleApiTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  property("Timezones on receipts are read accurately.") {
    fail("Hasn't been implemented")
  }

  private def formatDateInGmt(date: String) =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").parse(s"$date GMT")

  private def getDateTime(date: String): DateTime =
    new DateTime( formatDateInGmt(date) )
}
