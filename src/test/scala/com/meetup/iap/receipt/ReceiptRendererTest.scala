package com.meetup.iap.receipt

import com.meetup.scalacheck.ScalaTestPropertySpec
import com.meetup.iap.AppleApi
import AppleApi.{ReceiptResponse, ReceiptInfo}
import org.joda.time.{Period, DateTime}

class ReceiptRendererTest extends ScalaTestPropertySpec {

  property("Renderer should produce valid dates") {
    val purchaseDate = new DateTime().withMillis(0).toDate
    val expiresDate = new DateTime().withMillis(0).plus(Period.days(7)).toDate
    val cancellationDate = new DateTime().withMillis(0).plus(Period.days(3)).toDate

    println(s"Orig purchaseDate: $purchaseDate, $expiresDate, $cancellationDate")

    val transactionId = "10022345304"
    val receiptInfo = ReceiptInfo(
          purchaseDate,
          transactionId,
          transactionId,
          purchaseDate,
          expiresDate,
          "123943451",
          false,
          Some(cancellationDate),
          1)

    val json = ReceiptRenderer(ReceiptResponse(None, List(receiptInfo)))
    val response = AppleApi.parseResponse(json)

    response.latestInfo.isDefined should equal (true)
    response.latestInfo.map { info =>
      info.purchaseDate should equal (purchaseDate)
      info.expiresDate should equal (expiresDate)

      info.cancellationDate.isDefined should equal (true)
      info.cancellationDate.map(_ should equal (cancellationDate))
    }
  }
}
