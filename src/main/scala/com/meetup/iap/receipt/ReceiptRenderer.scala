package com.meetup.iap.receipt

import java.text.SimpleDateFormat

import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}

import java.util.{Date, TimeZone}

import org.joda.time.{DateTimeZone, DateTime}
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.JValue
import org.slf4j.LoggerFactory

object ReceiptRenderer {
  val log = LoggerFactory.getLogger(ReceiptRenderer.getClass)
  val EasternTZ = "US/Eastern"
  val GMTTZ = "GMT"
  def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  private def asGmt(date: Date): Date = translateTime(date, EasternTZ, GMTTZ)

  def apply(response: ReceiptResponse): String = {
    pretty(render(
      ("status" -> response.statusCode) ~
        ("latest_receipt_info" -> response.latestReceiptInfo.reverse.map(renderReceipt)) ~
        ("latest_receipt" -> response.latestReceipt)))
  }

  private def renderReceipt(receiptInfo: ReceiptInfo): JValue = {

    val gmtOrigPurchaseDate = asGmt(receiptInfo.originalPurchaseDate)
    val origPurchaseDate = sdf.format(gmtOrigPurchaseDate)
    val origPurchaseDateMs = gmtOrigPurchaseDate.getTime

    val gmtPurchaseDate = asGmt(receiptInfo.purchaseDate)
    val purchaseDate = sdf.format(gmtPurchaseDate)
    val purchaseDateMs = gmtPurchaseDate.getTime

    val gmtExpiresDate = asGmt(receiptInfo.expiresDate)
    val expiresDate = sdf.format(gmtExpiresDate)
    val expiresDateMs = gmtExpiresDate.getTime

    val cancellationDate = receiptInfo.cancellationDate.map { date =>
      val gmt = asGmt(date)
      sdf.format(gmt)
    }
    ("quantity" -> "1") ~
      ("product_id" -> receiptInfo.productId) ~
      ("transaction_id" -> receiptInfo.transactionId) ~
      ("original_transaction_id" -> receiptInfo.originalTransactionId) ~
      ("purchase_date" -> s"$purchaseDate Etc/GMT") ~
      ("purchase_date_ms" -> purchaseDateMs.toString) ~
      ("original_purchase_date" -> s"$origPurchaseDate Etc/GMT") ~
      ("original_purchase_date_ms" -> origPurchaseDateMs.toString) ~
      ("expires_date" -> s"$expiresDate Etc/GMT") ~
      ("expires_date_ms" -> expiresDateMs.toString) ~
      ("is_trial_period" -> receiptInfo.isTrialPeriod) ~
      ("cancellation_date" -> cancellationDate.map(d => s"$d Etc/GMT"))
  }

  def translateTime(d: Date, tzIn: String, tzOut: String): Date = {
    if (tzIn == tzOut) d
    else {
      try {
        val from = TimeZone.getTimeZone(tzIn)
        val to = TimeZone.getTimeZone(tzOut)
        val fromTime = new DateTime(d.getTime).withZoneRetainFields(DateTimeZone.forTimeZone(from))
        val toTime = fromTime.withZone(DateTimeZone.forTimeZone(to))

        toTime.withZoneRetainFields(DateTimeZone.forID(EasternTZ)).toDate
      } catch {
        case e: IllegalArgumentException =>
          log.error(s"Error converting time from $tzIn to $tzOut")
          d
      }
    }
  }
}
