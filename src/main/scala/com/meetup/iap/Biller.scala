package com.meetup.iap

import com.meetup.db.adapter.OrgPlanAdapter
import com.meetup.db.OrgSubQueries
import com.meetup.iap.receipt.{ReceiptGenerator, Subscription}
import com.meetup.util.Logging

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.{Map => CMap}
import scala.collection.JavaConverters._
import com.meetup.iap.AppleApi.ReceiptInfo
import java.util.Date

case class Plan(
    name: String,
    description: String,
    billInterval: Int,
    billIntervalUnit: String,
    trialInterval: Int,
    trialIntervalUnit: String,
    productId: String)

object Biller extends Logging {

  lazy val jsonPlans: List[Plan] = {
    log.info("Fetching NEW plans...")
    BillerCache.readPlansFromFile()
  }

  lazy val plansByProductId: Map[String, Plan] = jsonPlans.map { p => p.productId -> p }.toMap

  private val _subscriptions: CMap[String, Subscription] =
    new ConcurrentHashMap[String, Subscription].asScala

  def subscriptions = _subscriptions.toMap

  def createSub(plan: Plan, status: Int): Subscription = {
    val receiptEncoding = ReceiptGenerator.genEncoding(plan, subscriptions.keySet)
    val (_, receiptInfo) = ReceiptGenerator(plan, Left(receiptEncoding))
    val sub = Subscription(receiptEncoding, receiptInfo, status)

    _subscriptions.put(receiptEncoding, sub)
    BillerCache.writeToCache(subscriptions)
    sub
  }

  def renewSub(sub: Subscription, status: Int = 0) {
    plansByProductId.get(sub.latestReceiptInfo.productId).map { plan =>
      val (latestReceiptToken, latestReceiptInfo) = ReceiptGenerator(plan, Right(sub))
      val updatedSub = sub.addReceipt(latestReceiptInfo, status, latestReceiptToken)
      _subscriptions.put(sub.receiptToken, updatedSub)
	
      BillerCache.writeToCache(subscriptions)
    }
  }

  def cancelSub(sub: Subscription) {
    _subscriptions.put(sub.receiptToken, sub.cancel())
    BillerCache.writeToCache(subscriptions)
  }

  def refundTransaction(sub: Subscription, receiptInfo: ReceiptInfo) {
    log.info(s"Refunding transaction: ${receiptInfo.transactionId}")
    _subscriptions.put(sub.receiptToken, sub.refund(receiptInfo))
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
    jsonPlans
  }

//  LocalTimer.repeat(Period.seconds(10)) {
//    log.debug("doing stuff.")
//  }
}
