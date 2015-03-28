package com.meetup.iap

import unfiltered.filter.Planify
import unfiltered.request._
import unfiltered.response._
import unfiltered.directives._

import org.json4s.JsonAST.{JString, JInt}
import org.json4s.JsonDSL._
import org.json4s.JValue
import org.json4s.native.JsonMethods._

import scala.util.Try
import com.meetup.util.Logging
import scala.io.Source

object IAPPlan extends Logging {
  sealed trait OrgPlan
  case class OrgPlanName(name: String) extends OrgPlan
  case class OrgPlanId(id: Int) extends OrgPlan

  def getOrBad[T](opt: Option[T]) =
    opt.map(Directives.success).getOrElse(Directives.failure(BadRequest))

  def getOrgPlan(json: JValue) = {
    val orgPlan: Option[OrgPlan] = (json \ "org_plan_name" match {
      case JString(name) => Some(OrgPlanName(name))
      case _ => None
    }).orElse {
      json \ "org_plan_id" match {
        case JString(id) => Try(id.toInt).map(OrgPlanId).toOption
        case JInt(id) => Some(OrgPlanId(id.toInt))
        case _ => None
      }
    }

    getOrBad(orgPlan)
  }

  def plan = Planify { Directive.Intent {
    case GET(Path("/")) => Directives.success(page)

    case GET(Path("/receipts")) => Directives.success {
      JsonContent ~> ResponseString("")
    }

    case GET(Path("/plans")) => Directives.success {
      val json = pretty(render(Biller.plans.map { plan =>
        ("id" -> plan.getOrgPlanId.toInt) ~
          ("name" -> plan.getName) ~
          ("description" -> plan.getDescription)
      }))

      log.info(s"Serving up ${Biller.plans.size} plans")
      JsonContent ~> ResponseString(json)
    }

    // curl -d '{"org_plan_name":""}' http://localhost:9090/createReceipt
    // curl -d '{"org_plan_id":""}' http://localhost:9090/createReceipt
    case req @ POST(Path("/createReceipt")) =>
      for {
        json <- getOrBad(parseOpt(Body.string(req)))
        orgPlan <- getOrgPlan(json)
      } yield {
        orgPlan match {
          case OrgPlanId(id) =>
          case OrgPlanName(name) =>
        }

        ResponseString(orgPlan.toString)
      }

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

  def page = {
    val stream = getClass.getResourceAsStream("/template/page.html")
    val html = Source.fromInputStream(stream).mkString
    ResponseString(html)
  }
}
