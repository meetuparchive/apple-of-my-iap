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
      ("status" -> response.statusCode) ~
        ("latest_receipt_info" -> response.latestReceiptInfo.reverse.map(renderReceipt)) ~
        ("latest_receipt" -> response.latestReceipt)))
  }

  private def renderReceipt(receiptInfo: ReceiptInfo): JValue = {

    val gmtOrigPurchaseDate = asGmt(receiptInfo.originalPurchaseDate)
    val origPurchaseDate = TimeUtil.getFormattedDate(gmtOrigPurchaseDate, TimeUtil.timedateFormat)
    val origPurchaseDateMs = gmtOrigPurchaseDate.getTime

    val gmtPurchaseDate = asGmt(receiptInfo.purchaseDate)
    val purchaseDate = TimeUtil.getFormattedDate(gmtPurchaseDate, TimeUtil.timedateFormat)
    val purchaseDateMs = gmtPurchaseDate.getTime

    val gmtExpiresDate = asGmt(receiptInfo.expiresDate)
    val expiresDate = TimeUtil.getFormattedDate(gmtExpiresDate, TimeUtil.timedateFormat)
    val expiresDateMs = gmtExpiresDate.getTime

    val cancellationDate = receiptInfo.cancellationDate.map { date =>
      val gmt = asGmt(receiptInfo.purchaseDate)
      TimeUtil.getFormattedDate(gmt, TimeUtil.timedateFormat)
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
}
