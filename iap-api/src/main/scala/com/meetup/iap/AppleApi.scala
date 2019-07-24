package com.meetup.iap

import java.util.{Date, TimeZone}

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
    override def dateFormatter = {
      val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Etc/GMT'")
      format.setTimeZone(TimeZone.getTimeZone("GMT"))
      format
    }
  }

  /**
   * @param receipt Actual receipt info for the request receipt (original)
   * @param latestReceipt Latest receipt charged in a subscription.
   * @param latestExpiredReceiptInfo Latest receipt charged in an expired subscription.
   */
  case class ReceiptResponse(
    latestReceipt: Option[String] = None,
    latestReceiptInfo: List[ReceiptInfo] = List.empty,
    statusCode: Int = 0) {
    val latestInfo: Option[ReceiptInfo] = latestReceiptInfo.reverse.headOption
  }

  /**
    * Apple/iTunes doesn't follow the standard JSON specs and encodes some booleans and integers as Strings.
    * Specifically, it encodes "is_trial_period":"true" and "quantity":"1".
    *
    * In order to be able to parse their JSON into case objects, but still retain our rich types, we
    * maintain an internal model for deserializing Apple's JSON, then convert it to an external model
    * for clients of the API to use. (internal models: AppleReceiptResponse, AppleReceiptInfo)
    *
    * @param latestReceipt Actual receipt info for the request receipt
    * @param latestReceiptInfo Latest receipt charged in a subscription
    * @param status status of the iTunes customer
    */
  private case class AppleReceiptResponse(
    latestReceipt: Option[String],
    latestReceiptInfo: List[AppleReceiptInfo],
    status: Int)


  case class ReceiptInfo(
    originalPurchaseDate: Date,
    originalTransactionId: String,
    transactionId: String,
    purchaseDate: Date,
    expiresDate: Date,
    productId: String,
    isTrialPeriod: Boolean,
    isInIntroOfferPeriod: Option[Boolean],
    cancellationDate: Option[Date],
    quantity: Int)

  private case class AppleReceiptInfo(
    originalPurchaseDate: Date,
    originalTransactionId: String,
    transactionId: String,
    purchaseDate: Date,
    expiresDate: Date,
    productId: String,
    isTrialPeriod: String,
    isInIntroOfferPeriod: Option[String] = None,
    cancellationDate: Option[Date] = None,
    quantity: String)

  def parseResponse(json: String): ReceiptResponse = {
    val appleResponse = parseAppleResponse(json)
    convertAppleReceiptResponse(appleResponse)
  }

  private def convertAppleReceiptResponse(appleReceiptResponse: AppleReceiptResponse): ReceiptResponse = {
    ReceiptResponse(
      latestReceipt = appleReceiptResponse.latestReceipt,
      latestReceiptInfo = appleReceiptResponse.latestReceiptInfo.map(convertAppleReceiptInfo),
      statusCode = appleReceiptResponse.status
    )
  }

  private def convertAppleReceiptInfo(appleReceiptInfo: AppleReceiptInfo): ReceiptInfo = {
    ReceiptInfo(
      originalPurchaseDate = appleReceiptInfo.originalPurchaseDate,
      originalTransactionId = appleReceiptInfo.originalTransactionId,
      transactionId = appleReceiptInfo.transactionId,
      purchaseDate = appleReceiptInfo.purchaseDate,
      expiresDate = appleReceiptInfo.expiresDate,
      productId = appleReceiptInfo.productId,
      isTrialPeriod = appleReceiptInfo.isTrialPeriod.toBoolean, //we'll let this blow up if string value not convertable
      isInIntroOfferPeriod = appleReceiptInfo.isInIntroOfferPeriod.map(_.toBoolean),
      cancellationDate = appleReceiptInfo.cancellationDate,
      quantity = appleReceiptInfo.quantity.toInt
    )
  }

  private def parseAppleResponse(json: String): AppleReceiptResponse =
    read[AppleReceiptResponse](compact(render(parse(json).camelizeKeys)))

  /** Safely extract a receipt response from a parsed JSON structure. */
  def parseResponse(j: JValue): Either[Either[String, Status], ReceiptResponse] =
    j \ "status" match {
      case JInt(code) =>
        Status.get(code.toInt) match {
          case ValidReceipt =>
            val appleResponse = read[AppleReceiptResponse](compact(render(j.camelizeKeys)))
            Right(convertAppleReceiptResponse(appleResponse))
          case s => Left(Right(s))
        }
      case _ => Left(Left("Invalid JSON"))
    }
}
