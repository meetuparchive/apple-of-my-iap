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
  def addReceipt(receipt: ReceiptInfo) = this.copy(receipts = receipt :: receipts)
  def cancel() = {
    val cancellationDate = Some(new Date())
    this.copy(
      originalReceipt = originalReceipt.copy(cancellationDate = cancellationDate),
      status = Subscription.Status.Cancelled )
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
