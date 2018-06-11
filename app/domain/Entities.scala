package domain

import java.util.UUID

import org.joda.time.DateTime

/**
  * Value object to store price. Includes "amount" and "currency"
  *
  * @param amount   value representing amount of money
  * @param currency currency used
  */
case class Money(amount: BigDecimal, currency: String)

case class Offer(
  id: UUID,
  description: String,
  price: Money,
  expiration: Option[DateTime]
)

/**
  * Exception but also a message type indicating the offer with specified id could not be found.
  *
  * @param id offer identifier
  */
class OfferNotFound(id: UUID) extends RuntimeException(s"Offer $id could not be found") {}

/**
  * Exception but also a message type indicating the offer with specified id already exists and cannot be created.
  *
  * @param id offer identifier
  */
class OfferAlreadyExists(id: UUID) extends RuntimeException(s"Offer $id already exists") {}
