package com.meetup.iap

import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._

import org.json4s.native.JsonMethods._
import org.json4s.JsonAST.{JString, JField}

object IAPPlan extends Templates {

  def getOrBad[T](opt: Option[T]) =
    opt.map(Directives.success).getOrElse(Directives.failure(BadRequest))

  def plan = Planify { Directive.Intent {
    case GET(Path("/")) => Directives.success(index)

    //curl -d '{"receipt-data":"abcd"}' http://localhost:9090/verifyRceipt
    case req @ POST(Path("/verifyReceipt")) => {
      for {
        json <- getOrBad(parseOpt(Body.string(req)))
      } yield {
        json \ "receipt-data" match {
          case JString(receipt) => ResponseString(receipt)
          case _ => BadRequest
        }
      }
    }
  } }
}
