package com.meetup.iap

import unfiltered.filter.Plan
import unfiltered.request._
import unfiltered.response._

object IAPPlan extends Plan {
  def intent = {
    case POST(_) => ResponseString("""
{
  "status": 0,
  "receipt": {
    "item_id": "521129812",
    "bid": "com.meetup.Meetup",
    "purchase_date_pst": "2012-04-30 08:05:55 America/Los_Angeles",
    "original_purchase_date": "2012-04-30 15:05:55 Etc/GMT",
    "purchase_date": "2012-04-30 15:05:55 Etc/GMT",
    "original_purchase_date_pst": "2012-04-30 08:05:55 America/Los_Angeles",
    "original_transaction_id": "1000000046178817",
    "original_purchase_date_ms": "1335798355868",
    "transaction_id": "1000000046178817",
    "quantity": "1",
    "product_id": "com.meetup.download",
    "bvrs": "20120427",
    "purchase_date_ms": "1335798355868"
  }
}
""")
  }
}
