package com.meetup.iap.receipt

import com.meetup.base.util.TimeUtil
import com.meetup.business.iap.AppleApi.{ReceiptResponse, ReceiptInfo}

import java.util.Date

import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.JValue

object ReceiptRenderer {
  private def asGmt(date: Date): Date = TimeUtil.translateTime(date, TimeUtil.MEETUP_TZ, TimeUtil.GMT_TZ)

  def apply(response: ReceiptResponse): String = {
    pretty(render(
      ("receipt" -> renderReceipt(response.receipt)) ~
      ("latest_receipt_info" -> response.latestReceiptInfo.map(renderReceipt)) ~
      ("latest_expired_receipt_info" -> response.latestExpiredReceiptInfo.map(renderReceipt)) ))
  }

  private def renderReceipt(receiptInfo: ReceiptInfo): JValue = {

    val gmtOrigPurchaseDate = asGmt(receiptInfo.originalPurchaseDate)
    val origPurchaseDate = TimeUtil.getFormattedDate(gmtOrigPurchaseDate, TimeUtil.timedateFormat)
    val origPurchaseDateMs = gmtOrigPurchaseDate.getTime

    val gmtPurchaseDate = asGmt(receiptInfo.purchaseDate)
    val purchaseDate = TimeUtil.getFormattedDate(gmtPurchaseDate, TimeUtil.timedateFormat)
    val purchaseDateMs = gmtPurchaseDate.getTime

    parse(s"""{
      "item_id": "521129812",
      "bid": "com.meetup.Meetup",
      "product_id": "${receiptInfo.productId}",
      "transaction_id": "${receiptInfo.transactionId}",
      "purchase_date": "$purchaseDate Etc/GMT",
      "purchase_date_ms": "$purchaseDateMs"
      "original_transaction_id": "${receiptInfo.originalTransactionId}",
      "original_purchase_date": "$origPurchaseDate Etc/GMT",
      "original_purchase_date_ms": "$origPurchaseDateMs",
      "quantity": "1",
      "bvrs": "20120427",
    }""")
  }

}
