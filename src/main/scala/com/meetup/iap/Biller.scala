package com.meetup.iap

import com.meetup.db.OrgSubscriptionQueries
import com.meetup.iap.receipt.{ReceiptGenerator, Subscription}
import com.meetup.util.Logging

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

import com.meetup.db.adapter.OrgPlanAdapter

object Biller extends Logging {
  lazy val plans: Map[Int, OrgPlanAdapter] = {
    log.info("Fetching plans...")
    OrgSubscriptionQueries.getAllAppleOrgPlans.asScala
      .map(e => (e.getId.toInt, e))
      .toMap
  }

  lazy val plansByAppleRef: Map[String, OrgPlanAdapter] = plans.map { case (_,v) =>
    (v.getOrgPlanApple.getAppleItemRef, v)
  }.toMap

  private var _subscriptions: MMap[String, Subscription] = MMap.empty

  def subscriptions = _subscriptions.toMap

  def createSub(orgPlan: OrgPlanAdapter): Subscription = {
    val receiptEncoding = ReceiptGenerator.genEncoding(orgPlan, subscriptions.keySet)
    val receipt = ReceiptGenerator(orgPlan, Left(receiptEncoding))
    val sub = Subscription(receiptEncoding, receipt)

    _subscriptions.put(receiptEncoding, sub)
    BillerCache.writeToCache(_subscriptions)
    sub
  }

  def renewSub(sub: Subscription) {
    plansByAppleRef.get(sub.originalReceipt.productId).map { orgPlan =>
      val latestReceipt = ReceiptGenerator(orgPlan, Right(sub))
      val updatedSub = sub.addReceipt(latestReceipt)
      _subscriptions.put(sub.receipt, updatedSub)
      BillerCache.writeToCache(_subscriptions)
    }
  }

  def cancelSub(sub: Subscription) {
    _subscriptions.put(sub.receipt, sub.cancel())
    BillerCache.writeToCache(_subscriptions)
  }

  def clearSubs() = {
    _subscriptions = MMap()
    BillerCache.writeToCache(_subscriptions)
  }

  def shutdown() = {
//    LocalTimer.shutdown()
    BillerCache.writeToCache(_subscriptions)
  }

  def start() {
    log.info("Reading subs from cache.")
    _subscriptions = BillerCache.readFromCache()
    plans
  }

//  LocalTimer.repeat(Period.seconds(10)) {
//    log.debug("doing stuff.")
//  }
}
