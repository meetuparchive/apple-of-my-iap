package com.meetup.iap.client

import dispatch._, Defaults._
import com.meetup.iap.Status
import com.meetup.iap.AppleApi._
import com.ning.http.client.RequestBuilder
import org.apache.log4j.Logger
import scala.concurrent.{ExecutionContext, Future}
import org.json4s._
import org.json4s.native.JsonMethods._
import com.ning.http.client.Response

object Client {

  def live(http: Http = Http, password: String, logger: Logger)(implicit e: ExecutionContext) =
    cons(IAP.Live, http, logger, Some(password))

  def sandbox(http: Http = Http, password: String, logger: Logger)(implicit e: ExecutionContext) =
    cons(IAP.Sandbox, http, logger, Some(password))

  def other(url: String, http: Http = Http, logger: Logger)(implicit e: ExecutionContext = Defaults.executor) =
    cons(dispatch.url(url), http, logger)

  private def cons(rb: Req, http: Http = Http, logger: Logger, password: Option[String] = None)(implicit e: ExecutionContext) =
    new Client(rb, http, password, logger)(e)
}

final class Client private (rb: Req, http: Http = Http, password: Option[String] = None, logger: Logger)(implicit val ctx: ExecutionContext) {
  val base = rb / IAP.VerifyReceipt
  def verifyReceipt(receipt: String, logResponse: Boolean = false): Future[Either[Either[String, Status], ReceiptResponse]] = {
    val receiptDataParam = s""" "receipt-data" : "$receipt" """
    val jsonParams = password.fold(receiptDataParam)(pwd => s""" $receiptDataParam, "password" : "$pwd", "exclude-old-transactions": true """)
    val req = (base << s"""{ $jsonParams }""")
      .setHeader("Content-Type", "application/json; charset=UTF-8")
    http(req OK Json).map{ res =>
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


object Json extends (Response => JValue) {
  def apply(r: Response) =
    (dispatch.as.String andThen (s => parse(StringInput(s), true)))(r)
}
