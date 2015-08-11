package com.meetup.iap.client

import com.meetup.iap.Status

/** A unifying type for string and Status errors intended to be more readable,
   but it provides nothing not encoded as `Either[String, Status]`. */
sealed trait IAPError
final case class PayloadError(message: String) extends IAPError
final case class StatusError(status: Status) extends IAPError
