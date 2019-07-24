package com.meetup.iap.receipt

import java.text.SimpleDateFormat

import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}

import java.util.{Date, TimeZone}

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.JValue
import org.slf4j.LoggerFactory

object ReceiptRenderer {
  val log = LoggerFactory.getLogger(ReceiptRenderer.getClass)

  private def appleDateFormat(date: Date): String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Etc/GMT'")
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
    sdf.format(date)
  }

  def apply(response: ReceiptResponse): String = {
    pretty(render(
      ("status" -> response.statusCode) ~
        ("latest_receipt_info" -> response.latestReceiptInfo.reverse.map(renderReceipt)) ~
        ("latest_receipt" -> response.latestReceipt)))
  }

  private def renderReceipt(receiptInfo: ReceiptInfo): JValue = {
    val origPurchaseDate = receiptInfo.originalPurchaseDate
    val origPurchaseDateStr = appleDateFormat(origPurchaseDate)
    val origPurchaseDateMs = origPurchaseDate.getTime

    val purchaseDate = receiptInfo.purchaseDate
    val purchaseDateStr = appleDateFormat(purchaseDate)
    val purchaseDateMs = purchaseDate.getTime

    val expiresDate = receiptInfo.expiresDate
    val expiresDateStr = appleDateFormat(expiresDate)
    val expiresDateMs = expiresDate.getTime

    val cancellationDate = receiptInfo.cancellationDate.map { date =>
      appleDateFormat(date)
    }
    ("quantity" -> "1") ~
      ("product_id" -> receiptInfo.productId) ~
      ("transaction_id" -> receiptInfo.transactionId) ~
      ("original_transaction_id" -> receiptInfo.originalTransactionId) ~
      ("purchase_date" -> purchaseDateStr) ~
      ("purchase_date_ms" -> purchaseDateMs.toString) ~
      ("original_purchase_date" -> origPurchaseDateStr) ~
      ("original_purchase_date_ms" -> origPurchaseDateMs.toString) ~
      ("expires_date" -> expiresDateStr) ~
      ("expires_date_ms" -> expiresDateMs.toString) ~
      ("is_trial_period" -> receiptInfo.isTrialPeriod.toString) ~ //We mimic Apple's weird json here by converting the boolean type to a string
      ("is_in_intro_offer_period" -> receiptInfo.isInIntroOfferPeriod.map(_.toString)) ~
      ("cancellation_date" -> cancellationDate)
  }
}
