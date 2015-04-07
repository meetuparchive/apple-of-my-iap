package com.meetup.iap

import java.util.Date

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.read
import java.text.SimpleDateFormat

/**
 * Handles requests to apple's verify services and parsing of response.
 */
object AppleApi {

  implicit def formats = new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Etc/GMT'")
  }

  /**
   * @param receipt Actual receipt info for the request receipt (original)
   * @param latestReceipt Latest receipt charged in a subscription.
   * @param latestExpiredReceiptInfo Latest receipt charged in an expired subscription.
   */
  case class ReceiptResponse(
    receipt: ReceiptInfo,
    latestReceipt: Option[String] = None,
    latestReceiptInfo: Option[ReceiptInfo] = None,
    latestExpiredReceiptInfo: Option[ReceiptInfo] = None )

  case class ReceiptInfo(
    originalPurchaseDate: Date,
    originalTransactionId: String,
    transactionId: String,
    purchaseDate: Date,
    productId: String,
    receipt: Option[String] = None,
    cancellationDate: Option[Date] = None )

  def parseResponse(json: String): ReceiptResponse =
    read[ReceiptResponse](compact(render(parse(json).camelizeKeys)))
}
