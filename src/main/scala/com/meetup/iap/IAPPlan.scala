package com.meetup.iap

import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._

import org.json4s.JsonAST.{JString, JInt}
import org.json4s.JsonDSL._
import org.json4s.{DefaultFormats, JValue}
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.writePretty

import scala.util.Try
import com.meetup.util.Logging
import scala.io.Source
import com.meetup.iap.receipt.{ReceiptRenderer, ReceiptGenerator}

object IAPPlan extends Logging {
  implicit val formats = DefaultFormats

  private def getOrBad[T](opt: Option[T]) = Directives.getOrElse(opt, BadRequest)

  private def getOrgPlanId(json: JValue) = {
    val orgPlanId: Option[Int] =
      json \ "orgPlanId" match {
        case JString(id) => Try(id.toInt).toOption
        case JInt(id) => Some(id.toInt)
        case _ => None
    }

    getOrBad(orgPlanId)
  }

  private def getStatusCode(json: JValue) = {
    val statusCode: Option[Int] = 
      json \ "status" match {
        case JString(id) => Try(id.toInt).toOption
        case JInt(id) => Some(id.toInt)
        case _ => None 
      }

    getOrBad(statusCode)
  }

  private def getReceiptData(json: JValue) = {
    val receiptOpt = json \ "receipt-data" match {
      case JString(receipt) => Some(receipt)
      case _ => None
    }

    getOrBad(receiptOpt)
  }

  def plan = Planify { Directive.Intent {
    case GET(Path("/")) => Directives.success(page)

    case GET(Path("/plans")) => Directives.success {
        val json = pretty(render(
          Biller.plans.values.map { plan =>
            ("id" -> plan.getOrgPlanId.toInt) ~
            ("name" -> plan.getName) ~
            ("description" -> plan.getDescription)
        }))

      log.info(s"Serving up ${Biller.plans.size} plans")
      JsonContent ~> ResponseString(json)
    }

    case POST(Path("/subs/clear")) => Directives.success {
      Biller.clearSubs()
      JsonContent ~> Ok
    }

    // curl -d '' http://localhost:9090/receipts/abcd/renew
    case POST(Path(Seg("subs" :: receiptEncoded :: "renew" :: Nil))) =>
      for {
        sub <- getOrBad(Biller.subscriptions.get(receiptEncoded))
      } yield {
        Biller.renewSub(sub)
        JsonContent ~> Ok
      }

    case POST(Path(Seg("subs" :: receiptEncoded :: "cancel" :: Nil))) =>
      for {
        sub <- getOrBad(Biller.subscriptions.get(receiptEncoded))
      } yield {
        Biller.cancelSub(sub)
        JsonContent ~> Ok
      }


    case POST(Path(Seg("subs" :: receiptEncoded :: "refund" :: transactionId :: Nil))) =>
      for {
        sub <- getOrBad(Biller.subscriptions.get(receiptEncoded))
        receiptInfo <- getOrBad(sub.transactionMap.get(transactionId))
      } yield {
        Biller.refundTransaction(sub, receiptInfo)
        JsonContent ~> Ok
      }


    case GET(Path("/subs")) => Directives.success {
      val sortedSubs =
        Biller.subscriptions.values
          .toList
          .sortBy(_.originalReceipt.purchaseDate.getTime)
          .reverse

      JsonContent ~> ResponseString(writePretty(sortedSubs))
    }

    // curl -d '{"orgPlanId":"", "status":""}' http://localhost:9090/receipts
    case req @ POST(Path("/subs")) =>
      for {
        json <- getOrBad(parseOpt(Body.string(req)))
        orgPlanId <- getOrgPlanId(json)
        orgPlan <- getOrBad(Biller.plans.get(orgPlanId))
        status <- getStatusCode(json)
      } yield {
        log.info(s"Creating receipt for plan '${orgPlan.getName}' with status: $status")
        val sub = Biller.createSub(orgPlan, status)
        JsonContent ~> ResponseString(writePretty(sub))
      }

    //curl -d '{"receipt-data":"abcd"}' http://localhost:9090/verifyRceipt
    case req @ POST(Path("/verifyReceipt")) =>
      for {
        json <- getOrBad(parseOpt(Body.string(req)))
        receipt <- getReceiptData(json)
        sub <- getOrBad(Biller.subscriptions.get(receipt))
      } yield {
        val receiptResponse = ReceiptGenerator(sub)
        JsonContent ~> ResponseString(ReceiptRenderer(receiptResponse))
      }
  } }

  private def page = {
    val stream = getClass.getResourceAsStream("/template/page.html")
    val html = Source.fromInputStream(stream).mkString
    ResponseString(html)
  }
}
