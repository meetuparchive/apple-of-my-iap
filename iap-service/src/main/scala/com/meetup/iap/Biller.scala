package com.meetup.iap

import com.meetup.iap.receipt.{ReceiptGenerator, Subscription}
import org.slf4j.LoggerFactory

import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.{Map => CMap}
import scala.collection.JavaConverters._
import com.meetup.iap.AppleApi.ReceiptInfo

case class Plan(
    name: String,
    description: String,
    billInterval: Int,
    billIntervalUnit: String,
    trialInterval: Int,
    trialIntervalUnit: String,
    productId: String)

object Biller {
  val log = LoggerFactory.getLogger(Biller.getClass)
  lazy val plans: List[Plan] = {
    log.info("Fetching NEW plans...")
    BillerCache.readPlansFromFile()
  }

  lazy val plansByProductId: Map[String, Plan] = plans.map { p => p.productId -> p }.toMap

  private val _subscriptions: CMap[String, Subscription] =
    new ConcurrentHashMap[String, Subscription].asScala

  def subscriptions = _subscriptions.toMap

  def createSub(plan: Plan): Subscription = {
    val receiptEncoding = ReceiptGenerator.genEncoding(plan, subscriptions.keySet)
    val (_, receiptInfo) = ReceiptGenerator(plan, Left(receiptEncoding))
    val sub = Subscription(receiptEncoding, receiptInfo)

    _subscriptions.put(receiptEncoding, sub)
    BillerCache.writeToCache(subscriptions)
    sub
  }

  def setSubStatus(sub: Subscription, status: Int) {
    _subscriptions.put(sub.receiptToken, sub.copy(status = status))
    BillerCache.writeToCache(subscriptions)
  }

  def renewSub(sub: Subscription) {
    plansByProductId.get(sub.latestReceiptInfo.productId).map { plan =>
      val (latestReceiptToken, latestReceiptInfo) = ReceiptGenerator(plan, Right(sub))
      val updatedSub = sub.addReceipt(latestReceiptInfo, latestReceiptToken)
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
    plans
  }

//  LocalTimer.repeat(Period.seconds(10)) {
//    log.debug("doing stuff.")
//  }
}
