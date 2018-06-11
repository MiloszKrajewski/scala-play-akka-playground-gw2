package models

import domain.{Money, Offer}
import org.joda.time.DateTime
import java.util.UUID

/* Commands (ie: CreateOffer, GetOffer, etc), Events (ie: OfferCreated), and transfer objects (ie: OfferInfo) */

case class CreateOffer(
  description: String,
  price: Money,
  expiration: Option[DateTime]
)

case class OfferCreated(id: UUID)

case class CancelOffer(id: UUID)

case class GetOffers(all: Boolean)

case class GetOffer(id: UUID)

case class OfferInfo(
  id: UUID,
  description: String,
  price: Money,
  expiration: Option[DateTime],
  isExpired: Boolean
)

case class OfferInfoList(offers: List[OfferInfo])
