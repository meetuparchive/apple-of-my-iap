package com.meetup.iap

import com.meetup.db.OrgSubscriptionQueries
import com.meetup.iap.receipt.Subscription
import com.meetup.util.{Logging, LocalTimer}

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MMap}

import org.joda.time.Period

object Biller extends Logging {
  lazy val plans = {
    log.info("Fetching plans...")
    OrgSubscriptionQueries.getAllAppleOrgPlans.asScala
  }

  private var _subscriptions: MMap[String, Subscription] = MMap.empty

  def subscriptions = _subscriptions

  def shutdown() = {
    LocalTimer.shutdown()
    BillerCache.writeToCache(_subscriptions)
  }

  def start() {
    log.info("Reading subs from cache.")
    _subscriptions = BillerCache.readFromCache()
  }

  LocalTimer.repeat(Period.seconds(10)) {
    log.debug("doing stuff.")
  }
}
