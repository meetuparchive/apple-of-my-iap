package com.meetup.iap.receipt

import com.meetup.base.util.TimeUtil
import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.JValue

object ReceiptRenderer {
  private def asGmt(date: Date): Date = TimeUtil.translateTime(date, TimeUtil.MEETUP_TZ, TimeUtil.GMT_TZ)

  def apply(response: ReceiptResponse): String = {
    pretty(render(
      ("receipt" -> renderReceipt(response.receipt)) ~
        ("latest_receipt_info" -> response.latestReceiptInfo.map(renderReceipt)) ~
        ("latest_expired_receipt_info" -> response.latestExpiredReceiptInfo.map(renderReceipt)) ~
        ("status" -> response.statusCode) ~
        ("latestReceipt" -> response.latestReceipt)))
  }

  private def renderReceipt(receiptInfo: ReceiptInfo): JValue = {

    val gmtOrigPurchaseDate = asGmt(receiptInfo.originalPurchaseDate)
    val origPurchaseDate = TimeUtil.getFormattedDate(gmtOrigPurchaseDate, TimeUtil.timedateFormat)
    val origPurchaseDateMs = gmtOrigPurchaseDate.getTime

    val gmtPurchaseDate = asGmt(receiptInfo.purchaseDate)
    val purchaseDate = TimeUtil.getFormattedDate(gmtPurchaseDate, TimeUtil.timedateFormat)
    val purchaseDateMs = gmtPurchaseDate.getTime

    val cancellationDate = receiptInfo.cancellationDate.map { date =>
      val gmt = asGmt(receiptInfo.purchaseDate)
      TimeUtil.getFormattedDate(gmt, TimeUtil.timedateFormat)
    }

    ("original_transaction_id" -> receiptInfo.originalTransactionId) ~
      ("original_purchase_date" -> s"$origPurchaseDate Etc/GMT") ~
      ("original_purchase_date_ms" -> origPurchaseDateMs.toString) ~
      ("purchase_date" -> s"$purchaseDate Etc/GMT") ~
      ("purchase_date_ms" -> purchaseDateMs.toString) ~
      ("product_id" -> receiptInfo.productId) ~
      ("transaction_id" -> receiptInfo.transactionId) ~
      ("cancellation_date" -> cancellationDate.map(d => s"$d Etc/GMT")) ~
      // Just some dump generic stuff that you'll find in their response.
      ("item_id" -> "521129812") ~
      ("bid" -> "com.meetup.Meetup") ~
      ("quantity" -> "1") ~
      ("bvrs" -> "20120427")
  }
}
