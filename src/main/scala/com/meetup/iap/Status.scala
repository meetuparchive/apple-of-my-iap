package com.meetup.iap

// https://developer.apple.com/library/ios/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW5
sealed abstract class Status(val code: Int, val description: String) {
  def tuple = (code, this)
}
/** The submitted receipt is valid. All other statuses indicate some kind
    of error. */
case object ValidReceipt extends Status(0, "Valid receipt.")
case object BadEnvelope extends Status(21000, "The App Store could not read the JSON object you provided.")
case object BadReceipt extends Status(21002, "The data in the 'receipt-data' property was malformed or missing.")
case object UnauthorizedReceipt extends Status(21003, "The receipt could not be authenticated.")
/** Only returned for iOS 6 style transaction receipts for
    auto-renewable subscriptions. */
case object SharedSecretMismatch extends Status(21004, "The shared secret you provided does not match the shared secret on file for your account.")
case object ServerUnavailable extends Status(21005, "The receipt server is not currently available.")
/** When this status code is returned to your server, the receipt data is also
    decoded and returned as part of the response. Only returned for iOS 6
    style transaction receipts for auto-renewable subscriptions. */
case object SubscriptionExpired extends Status(21006, "This receipt is valid but the subscription has expired.")
case object TestToProd extends Status(21007, "This receipt is from the test environment, but it was sent to the production environment for verification. Send it to the test environment instead.")
case object ProdToTest extends Status(21008, "This receipt is from the production environment, but it was sent to the test environment for verification. Send it to the production environment instead")
/** This is a stand-in for undocumented or new status codes. */
final case class Unknown private[iap] (override val code: Int) extends Status(code, "Unknown status.")

object Status {
  /** All defined statuses. */
  val defined = Map(
    ValidReceipt.tuple,
    BadEnvelope.tuple,
    BadReceipt.tuple,
    UnauthorizedReceipt.tuple,
    SharedSecretMismatch.tuple,
    ServerUnavailable.tuple,
    SubscriptionExpired.tuple,
    TestToProd.tuple,
    ProdToTest.tuple
  )

  /** Given a status code, resolve a defined status or fall back to unknown. */
  def get(code: Int) =
    defined.getOrElse(code, Unknown(code))
}
