package com.meetup.iap.receipt

import com.meetup.scalacheck.ScalaTestPropertySpec
import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}
import org.joda.time.DateTime

class ReceiptRendererTest extends ScalaTestPropertySpec {

  property("Renderer should produce valid dates") {
    val purchaseDate = new DateTime().withMillis(0).toDate
    val originalPurchaseDate = purchaseDate

    val receipt = ReceiptInfo(
        originalPurchaseDate,
        "orig_trans_1233",
        "trans_1234",
        purchaseDate,
        "product_123"
    )

    val json = ReceiptRenderer(ReceiptResponse(receipt))
    val response = AppleApi.parseResponse(json)

    response.receipt.purchaseDate should equal (purchaseDate)
    response.receipt.originalPurchaseDate should equal (originalPurchaseDate)
  }

  property("Renderer should produce valid latest receipts.") {
    val purchaseDate = new DateTime().withMillis(0).minusMonths(1).toDate
    val originalPurchaseDate = purchaseDate

    val latestPurchaseDate = new DateTime().withMillis(0).toDate

    val receipt = ReceiptInfo(
      productId = "product_123",
      transactionId = "trans_1234",
      purchaseDate = purchaseDate,
      originalPurchaseDate = originalPurchaseDate,
      originalTransactionId = "orig_trans_1233"
    )

    val latestReceipt = ReceiptInfo(
      productId = "product_123",
      transactionId = "trans_1234",
      purchaseDate = latestPurchaseDate,
      originalPurchaseDate = originalPurchaseDate,
      originalTransactionId = "orig_trans_1233"
    )

    val json = ReceiptRenderer(ReceiptResponse(receipt, latestReceiptInfo = Some(latestReceipt)))
    val response = AppleApi.parseResponse(json)

    response.receipt.purchaseDate should equal (purchaseDate)
    response.receipt.originalPurchaseDate should equal (originalPurchaseDate)

    response.latestReceiptInfo.nonEmpty should be (true)
    response.latestReceiptInfo.map { latest =>
      latest.purchaseDate should equal(latestPurchaseDate)
    }
  }
}
