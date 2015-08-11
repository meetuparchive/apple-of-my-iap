package com.meetup.iap.receipt

import com.meetup.iap.AppleApi
import com.meetup.iap.Plan
import AppleApi.{ReceiptResponse, ReceiptInfo}
import org.joda.time.{DateTime, Period}

object ReceiptGenerator {

  def genEncoding(plan: Plan,
                  existingEncodings: Set[String]): String = {
    def helper: String = {
      val id = java.util.UUID.randomUUID.toString.split("-")
      val id1 = id(0)
      val id2 = id(1)
      val receipt = s"${plan.name}_$id1-$id2"

      if(existingEncodings.contains(receipt)) helper
      else receipt
    }

    helper
  }

  def apply(plan: Plan,
            receiptOrSub: Either[String, Subscription] ): (String, ReceiptInfo) = {

    val purchaseDateTime = new DateTime()
    val purchaseDate = purchaseDateTime.toDate
    val productId = plan.productId
    val transactionId = s"$productId-$purchaseDateTime"
    val expiresDate = calculateEndDate(purchaseDateTime, plan.billInterval, plan.billIntervalUnit).toDate

    val (origPurchaseDate, origTransId, receiptToken) = receiptOrSub match {
      case Left(receipt) =>
        (purchaseDate, transactionId, receipt)
      case Right(subscription) =>
        val orig = subscription.originalReceiptInfo
        val origReceipt = subscription.receiptTokenMap.get(orig.transactionId).getOrElse("ERROR_no_receipt_token_found")
        val id = subscription.receiptsList.size
        (orig.purchaseDate, orig.transactionId, f"$origReceipt-${id}%03d")
    }

    (receiptToken, ReceiptInfo(
                    origPurchaseDate,
                    origTransId,
                    transactionId,
                    purchaseDate,
                    expiresDate,
                    productId))
  }

  def apply(sub: Subscription): ReceiptResponse = {
    ReceiptResponse(
      sub.latestReceiptToken,
      sub.receiptsList,
      sub.status)
  }

  def calculateEndDate(startDate: DateTime, interval: Int, intervalUnit: String): DateTime = {
    startDate.plus(getPeriod(intervalUnit, interval))
  }

  private def getPeriod(interval: String, length: Int): Period = {
    interval match {
      case "seconds" => Period.seconds(length)
      case "minutes" => Period.minutes(length)
      case "hours"   => Period.hours(length)
      case "days"    => Period.days(length)
      case "weeks"   => Period.weeks(length)
      case "months"  => Period.months(length)
      case "years"   => Period.years(length)
      case _         => throw new IllegalStateException(s"Could not create period for interval: $interval")
    }
  }
}
