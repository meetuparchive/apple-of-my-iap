package com.meetup.iap.client

import dispatch._

/** Various IAP constants and utilities for constructing requests. */
object IAP {
  def Sandbox = url("https://sandbox.itunes.apple.com")
  def Live = url("https://buy.itunes.apple.com")

  val VerifyReceipt = "verifyReceipt"
}
