package com.meetup.iap.receipt

import com.meetup.db.adapter.OrgPlanAdapter
import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}
import org.joda.time.DateTime

object ReceiptGenerator {

  def genEncoding(orgPlan: OrgPlanAdapter,
                  existingEncodings: Set[String]): String = {
    def helper: String = {
      val id = java.util.UUID.randomUUID.toString.split("-")
      val id1 = id(0)
      val id2 = id(1)
      val receipt = s"${orgPlan.getName}_$id1-$id2"

      if(existingEncodings.contains(receipt)) helper
      else receipt
    }

    helper
  }

  def apply(orgPlan:OrgPlanAdapter,
            receiptOrSub: Either[String, Subscription] ): ReceiptInfo = {

    val purchaseDateTime = new DateTime()
    val purchaseDate = purchaseDateTime.toDate
    val productId = orgPlan.getOrgPlanApple.getAppleProductRef
    val transactionId = s"$productId-$purchaseDateTime"

    val (origPurchaseDate, origTransId, receipt) = receiptOrSub match {
      case Left(receipt) =>
        (purchaseDate, transactionId, receipt)
      case Right(subscription) =>
        val orig = subscription.originalReceipt
        val id = subscription.receipts.size
        (orig.purchaseDate, orig.transactionId, f"${orig.receipt.head}-${id}%03d")
    }

    ReceiptInfo(
      origPurchaseDate,
      origTransId,
      transactionId,
      purchaseDate,
      productId,
      Some(receipt) )
  }

  def apply(sub: Subscription): ReceiptResponse = {
    val origReceipt = sub.originalReceipt

    val latestReceiptInfo =
      sub.receipts.headOption
        .filter(_ => sub.subStatus == Subscription.Status.Active && sub.receipts.size > 1)

    val latestExpiredReceiptInfo =
      sub.receipts.headOption
        .filter(_ => sub.subStatus == Subscription.Status.Cancelled && sub.receipts.size > 1)

    ReceiptResponse(
      origReceipt,
      latestReceiptInfo.map(_.receipt).flatten,
      latestReceiptInfo,
      latestExpiredReceiptInfo, 
      sub.status )
  }
}
