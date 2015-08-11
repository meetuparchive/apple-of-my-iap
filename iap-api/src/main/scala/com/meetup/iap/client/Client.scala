package com.meetup.iap.client

import dispatch._
import dispatch.as.{json4s => asJson4s}
import com.meetup.iap.Status
import com.meetup.iap.AppleApi._
import com.ning.http.client.RequestBuilder
import org.apache.log4j.Logger
import org.json4s.native.JsonMethods._
import scala.concurrent.{ExecutionContext, Future}

object Client {

  def live(http: Http = Http, password: String, logger: Logger)(implicit e: ExecutionContext) =
    cons(IAP.Live, http, logger, Some(password))

  def sandbox(http: Http = Http, password: String, logger: Logger)(implicit e: ExecutionContext) =
    cons(IAP.Sandbox, http, logger, Some(password))

  def other(url: String, http: Http = Http, logger: Logger)(implicit e: ExecutionContext = Defaults.executor) =
    cons(dispatch.url(url), http, logger)

  private def cons(rb: RequestBuilder, http: Http = Http, logger: Logger, password: Option[String] = None)(implicit e: ExecutionContext) =
    new Client(rb, http, password, logger)(e)
}

final class Client private (rb: RequestBuilder, http: Http = Http, password: Option[String] = None, logger: Logger)(implicit val ctx: ExecutionContext) {
  val base = rb / IAP.VerifyReceipt
  def verifyReceipt(receipt: String, logResponse: Boolean = false): Future[Either[Either[String, Status], ReceiptResponse]] = {
    val receiptDataParam = s""" "receipt-data" : "$receipt" """
    val jsonParams = password.fold(receiptDataParam)(pwd => s""" $receiptDataParam, "password" : "$pwd" """)
    val req = (base << s"""{ $jsonParams }""")
      .setHeader("Content-Type", "application/json; charset=UTF-8")
    http(req OK asJson4s.Json).map{ res =>
      if (logResponse) {
        //optional logging
        logger.error(s"IAP Response: ${compact(render(res.camelizeKeys))}")
      }

      parseResponse(res)
    }
  }

  override def toString = rb.build().getUrl
  def getHttpConnection = http
}
