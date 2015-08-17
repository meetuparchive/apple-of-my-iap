package com.meetup.iap.receipt

import com.meetup.iap.{ValidReceipt, AppleApi}
import AppleApi.ReceiptInfo
import java.util.Date

/**
 *
 * @param receipt encoding receipt
 * @param originalReceipt
 * @param receipts
 */
case class Subscription(
    status: Int = ValidReceipt.code,
    receiptsList: List[ReceiptInfo],
    receiptToken: String,
    receiptTokenMap: Map[String, String], //receiptInfo -> receiptToken
    auto: Boolean = false,
    subStatus: String = Subscription.Status.Active ) {

  val transactionMap = receiptsList.map(r => r.transactionId -> r).toMap
  val originalReceiptInfo = receiptsList.last
  val latestReceiptInfo: ReceiptInfo = receiptsList.head
  val latestReceiptToken: Option[String] = receiptTokenMap.get(latestReceiptInfo.transactionId)

  def addReceipt(receipt: ReceiptInfo, newReceiptToken: String) =
    this.copy(
      receiptsList = receipt :: receiptsList,
      receiptTokenMap = receiptTokenMap + (receipt.transactionId -> newReceiptToken)
    )

  def cancel() = this.copy(subStatus = Subscription.Status.Cancelled)

  def refund(receiptInfo: ReceiptInfo) = {
    val cancellationDate = Some(new Date())
    val newReceipt = receiptInfo.copy(cancellationDate = cancellationDate)
    this.copy(receiptsList = receiptsList.map { r =>
        if (r.transactionId == newReceipt.transactionId) newReceipt else r
      })
    }
  }

object Subscription {
  object Status {
    val Active = "active"
    val Cancelled = "cancelled"
  }
  def apply(receiptToken: String, originalReceiptInfo: ReceiptInfo) =
    new Subscription(ValidReceipt.code, List(originalReceiptInfo), receiptToken, Map(originalReceiptInfo.transactionId -> receiptToken))
}
