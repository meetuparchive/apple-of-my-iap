package com.meetup.iap

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Date

class AppleApiTest extends PropSpec with PropertyChecks with Matchers {

  property("Timezones on receipts are read accurately.") {
    val responseSingle = AppleApi.parseResponse(Receipts.Single)
    val responseMultiple = AppleApi.parseResponse(Receipts.Multiple)

    val purchaseDate = getDateTime(Receipts.PurchaseDate)
    val expiresDate = getDateTime(Receipts.ExpiresDate)

    responseSingle.latestInfo.isDefined should equal (true)
    responseMultiple.latestInfo.isDefined should equal (true)

    for {
      singleInfo <- responseSingle.latestInfo
      multiInfo <- responseMultiple.latestInfo
    } {
      new DateTime(singleInfo.purchaseDate) should equal (purchaseDate)
      new DateTime(multiInfo.purchaseDate) should equal (purchaseDate)
      new DateTime(singleInfo.expiresDate) should equal (expiresDate)
      new DateTime(multiInfo.expiresDate) should equal (expiresDate)
    }
  }

  private def formatDateInGmt(date: String): Date =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").parse(s"$date GMT")

  private def getDateTime(date: String): DateTime =
    new DateTime( formatDateInGmt(date) )
}

object Receipts {
  val ProductId = "10000456703"
  val PurchaseDate = "2015-05-06 15:49:31"
  val ExpiresDate = "2015-05-06 15:50:16"
  
  val Single = s"""
  {
    "status": 0,
    "latest_receipt_info": [
      {
        "quantity": "1",
        "product_id": "$ProductId",
        "transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "original_transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "purchase_date": "$PurchaseDate Etc/GMT",
        "purchase_date_ms": "1430937814135",
        "original_purchase_date": "2015-05-06 14:43:34 Etc/GMT",
        "original_purchase_date_ms": "1430937814135",
        "expires_date": "$ExpiresDate Etc/GMT",
        "expires_date_ms": "1430937859135",
        "is_trial_period": false
      }
    ],
    "latest_receipt": "apple_45_seconds_plan_cc9c330e-3b26"
  }
  """

  val Multiple = s"""
  {
    "status": 0,
    "latest_receipt_info": [
      {
        "quantity": "1",
        "product_id": "$ProductId",
        "transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "original_transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "purchase_date": "2015-05-06 14:43:34 Etc/GMT",
        "purchase_date_ms": "1430937814135",
        "original_purchase_date": "2015-05-06 14:43:34 Etc/GMT",
        "original_purchase_date_ms": "1430937814135",
        "expires_date": "2015-05-06 14:44:19 Etc/GMT",
        "expires_date_ms": "1430937859135",
        "is_trial_period": false
      },
      {
        "quantity": "1",
        "product_id": "$ProductId",
        "transaction_id": "$ProductId-2015-05-06T11:49:29.701-04:00",
        "original_transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "purchase_date": "2015-05-06 15:49:29 Etc/GMT",
        "purchase_date_ms": "1430941769701",
        "original_purchase_date": "2015-05-06 14:43:34 Etc/GMT",
        "original_purchase_date_ms": "1430937814135",
        "expires_date": "2015-05-06 15:50:14 Etc/GMT",
        "expires_date_ms": "1430941814701",
        "is_trial_period": false
      },
      {
        "quantity": "1",
        "product_id": "$ProductId",
        "transaction_id": "$ProductId-2015-05-06T11:49:31.786-04:00",
        "original_transaction_id": "$ProductId-2015-05-06T10:43:34.135-04:00",
        "purchase_date": "$PurchaseDate Etc/GMT",
        "purchase_date_ms": "1430941771786",
        "original_purchase_date": "2015-05-06 14:43:34 Etc/GMT",
        "original_purchase_date_ms": "1430937814135",
        "expires_date": "$ExpiresDate Etc/GMT",
        "expires_date_ms": "1430941816786",
        "is_trial_period": false
      }
    ],
    "latest_receipt": "apple_45_seconds_plan_cc9c330e-3b26-002"
    }
  """
}
