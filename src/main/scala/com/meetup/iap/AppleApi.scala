package com.meetup.iap

import java.util.Date

import org.json4s.{DefaultFormats, JValue}
import org.json4s.JsonAST.JInt
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
    latestExpiredReceiptInfo: Option[ReceiptInfo] = None,
    statusCode: Int = 0)

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

  /** Safely extract a receipt response from a parsed JSON structure. */
  def parseResponse(j: JValue): Either[Either[String, Status], ReceiptResponse] =
    j \ "status" match {
      case JInt(code) =>
        Status.get(code.toInt) match {
          case ValidReceipt => Right(read[ReceiptResponse](compact(render(j.camelizeKeys))))
          case s => Left(Right(s))
        }
      case _ => Left(Left("Invalid JSON"))
    }
}
