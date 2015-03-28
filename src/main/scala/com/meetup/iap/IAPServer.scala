package com.meetup.iap

import unfiltered.jetty.Server

object IAPServer extends App {
  unfiltered.jetty.Http(9090)
    .resources(getClass().getResource("/web"))
    .plan(IAPPlan.plan)
    .run( _ => Biller.start(), _ => Biller.shutdown())
}
