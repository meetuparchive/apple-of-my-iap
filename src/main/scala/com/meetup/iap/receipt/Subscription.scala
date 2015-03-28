package com.meetup.iap.receipt

import com.meetup.business.iap.AppleApi.ReceiptInfo

case class Subscription(originalReceipt: ReceiptInfo, receipts: List[ReceiptInfo]) {
  def addReceipt(receipt: ReceiptInfo) = this.copy(receipts = receipt :: receipts)
}
