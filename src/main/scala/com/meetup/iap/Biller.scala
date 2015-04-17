package com.meetup.iap

import com.meetup.db.adapter.OrgPlanAdapter
import com.meetup.db.OrgSubscriptionQueries
import com.meetup.iap.receipt.{ReceiptGenerator, Subscription}
import com.meetup.util.Logging

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.{Map => CMap}
import scala.collection.JavaConverters._
import com.meetup.iap.AppleApi.ReceiptInfo
import java.util.Date

object Biller extends Logging {
  lazy val plans: Map[Int, OrgPlanAdapter] = {
    log.info("Fetching plans...")
    OrgSubscriptionQueries.getAllAppleOrgPlans.asScala
      .map(e => (e.getId.toInt, e))
      .toMap
  }

  lazy val plansByAppleRef: Map[String, OrgPlanAdapter] = plans.map { case (_,v) =>
    (v.getOrgPlanApple.getAppleProductRef, v)
  }.toMap

  private val _subscriptions: CMap[String, Subscription] =
    new ConcurrentHashMap[String, Subscription].asScala

  def subscriptions = _subscriptions.toMap

  def createSub(orgPlan: OrgPlanAdapter, status: Int): Subscription = {
    val receiptEncoding = ReceiptGenerator.genEncoding(orgPlan, subscriptions.keySet)
    val receipt = ReceiptGenerator(orgPlan, Left(receiptEncoding))
    val sub = Subscription(receiptEncoding, receipt, status)

    _subscriptions.put(receiptEncoding, sub)
    BillerCache.writeToCache(subscriptions)
    sub
  }

  def renewSub(sub: Subscription, status: Int = 0) {
    plansByAppleRef.get(sub.originalReceipt.productId).map { orgPlan =>
      val latestReceipt = ReceiptGenerator(orgPlan, Right(sub))
      val updatedSub = sub.addReceipt(latestReceipt, status)
      _subscriptions.put(sub.receipt, updatedSub)
	
      BillerCache.writeToCache(subscriptions)
    }
  }

  def cancelSub(sub: Subscription) {
    _subscriptions.put(sub.receipt, sub.cancel())
    BillerCache.writeToCache(subscriptions)
  }

  def refundTransaction(sub: Subscription, receiptInfo: ReceiptInfo) {
    log.info(s"Refunding transaction: ${receiptInfo.transactionId}")
    _subscriptions.put(sub.receipt, sub.refund(receiptInfo))
    BillerCache.writeToCache(subscriptions)
  }

  def clearSubs() = {
    _subscriptions.clear()
    BillerCache.writeToCache(subscriptions)
  }

  def shutdown() = {
//    LocalTimer.shutdown()
    BillerCache.writeToCache(subscriptions)
  }

  def start() {
    log.info("Reading subs from cache.")
    BillerCache.readFromCache().foreach { case (k,v) => _subscriptions.put(k,v) }
    plans
  }

//  LocalTimer.repeat(Period.seconds(10)) {
//    log.debug("doing stuff.")
//  }
}
