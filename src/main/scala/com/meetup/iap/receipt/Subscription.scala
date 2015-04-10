package com.meetup.iap.receipt

import com.meetup.iap.AppleApi
import AppleApi.ReceiptInfo
import java.util.Date

/**
 *
 * @param receipt encoding receipt
 * @param originalReceipt
 * @param receipts
 */
case class Subscription(
    receipt: String,
    originalReceipt: ReceiptInfo,
    receipts: List[ReceiptInfo],
    auto: Boolean = false,
    status: String = Subscription.Status.Active ) {

  val transactionMap = receipts.map(r => r.transactionId -> r).toMap + (originalReceipt.transactionId -> originalReceipt)
  def addReceipt(receipt: ReceiptInfo) = this.copy(receipts = receipt :: receipts)
  def cancel() = {
    this.copy(status = Subscription.Status.Cancelled)
  }

  def refund(receiptInfo: ReceiptInfo) = {
    val cancellationDate = Some(new Date())
    val newReceipt = receiptInfo.copy(cancellationDate = cancellationDate)
    if (receiptInfo.transactionId == originalReceipt.transactionId) this.copy(originalReceipt = newReceipt)
    else {
      this.copy(receipts = receipts.map { r =>
          if (r.transactionId == newReceipt.transactionId) newReceipt else r
      })
    }
  }
}

object Subscription {
  object Status {
    val Active = "active"
    val Cancelled = "cancelled"
  }
  def apply(receipt: String, originalReceipt: ReceiptInfo) =
    new Subscription(receipt, originalReceipt, List(originalReceipt))
}
