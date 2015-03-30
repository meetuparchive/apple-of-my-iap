package com.meetup.iap

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.joda.time.DateTime
import java.text.SimpleDateFormat

class AppleApiTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  property("Single receipt responses are parseable.") {
    val response = AppleApi.parseResponse(Receipts.Single)
    response.receipt.productId should equal (Receipts.ProductId)
  }

  property("Single cancelled (refunded) receipt response are parseable.") {
    val response = AppleApi.parseResponse(Receipts.SingleRefunded)

    response.receipt.cancellationDate.nonEmpty should be (true)
    response.receipt.cancellationDate.map( responseDate => {
      val date = getDateTime(Receipts.CancelTime)
      new DateTime(responseDate) should equal (date)
    })
  }

  property("Latest receipt responses are parseable.") {
    val response = AppleApi.parseResponse(Receipts.MonthLater)

    response.latestReceiptInfo.nonEmpty should be (true)
    response.latestReceiptInfo.map { latestInfo =>
      latestInfo.productId should equal (Receipts.ProductId)
    }
  }

  property("Latest expired receipt responses are parseable.") {
    val response = AppleApi.parseResponse(Receipts.Cancelled)

    response.latestExpiredReceiptInfo.nonEmpty should be (true)
    response.latestExpiredReceiptInfo.map { expiredInfo =>
      expiredInfo.productId should equal (Receipts.ProductId)
    }
  }

  property("Timezones on receipts are read accurately.") {
    val response = AppleApi.parseResponse(Receipts.Single)

    val time = getDateTime(Receipts.PurchaseTime)
    val responseTime = new DateTime(response.receipt.originalPurchaseDate)

    responseTime should equal (time)
  }

  private def formatDateInGmt(date: String) =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").parse(s"$date GMT")

  private def getDateTime(date: String): DateTime =
    new DateTime( formatDateInGmt(date) )
}

object Receipts {
  val ProductId = "com.meetup.purchase_1"
  val PurchaseTime = "2012-04-30 15:05:55"
  val RenewedPurchaseTime = "2012-05-30 15:05:55"
  val CancelTime = "2012-05-10 15:05:55"

  val Single = s"""
      { "receipt": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$PurchaseTime Etc/GMT",
        "original_purchase_date": "$PurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812"
      } }"""

  val SingleRefunded = s"""
      { "receipt": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$PurchaseTime Etc/GMT",
        "original_purchase_date": "$PurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812",
        "cancellation_date": "$CancelTime Etc/GMT"
      } }"""

  val MonthLater = s"""
      { "receipt": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$PurchaseTime Etc/GMT",
        "original_purchase_date": "$PurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812"
      },
      "latest_receipt" : "ASDJFADLSFKNASLDKFNASDLKFNASDLKFNASDLIFNASLINF",
      "latest_receipt_info": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$RenewedPurchaseTime Etc/GMT",
        "original_purchase_date": "$RenewedPurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812"
       } }"""

  val Cancelled = s"""
      { "receipt": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$PurchaseTime Etc/GMT",
        "original_purchase_date": "$PurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812"
      },
      "latest_expired_receipt_info": {
        "original_transaction_id": "1000000046178817",
        "original_purchase_date_ms": "1335798355868",
        "transaction_id": "1000000046178817",
        "quantity": "1",
        "product_id": "$ProductId",
        "bvrs": "20120427",
        "purchase_date_ms": "1335798355868",
        "purchase_date": "$RenewedPurchaseTime Etc/GMT",
        "original_purchase_date": "$RenewedPurchaseTime Etc/GMT",
        "bid": "br.com.jera.Example",
        "item_id": "521129812"
       } }"""

}
