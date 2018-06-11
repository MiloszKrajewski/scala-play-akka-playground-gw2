package domain

import java.util.UUID

import javax.inject.Inject
import models._
import org.joda.time.DateTime
import services.{OfferRepository, TimeProvider, UuidGenerator}

/**
  * Offers aggregate coordinating all actions related to offers.
  *
  * @param idGen      identifier generator
  * @param repository offer repository
  * @param time       time provider (please note, allows to use virtual time for testing)
  */
class OfferAggregate @Inject()(idGen: UuidGenerator, repository: OfferRepository, time: TimeProvider) {
  private def isExpired(offer: Offer, now: DateTime): Boolean =
    offer.expiration.exists { _.compareTo(now) <= 0 } // checks if date passed

  private def toOfferInfo(offer: Offer, now: DateTime): OfferInfo =
    OfferInfo(offer.id, offer.description, offer.price, offer.expiration, isExpired(offer, now))

  /**
    * Creates new offer and saves it to repository
    *
    * @param request creation request
    * @return offer
    */
  def create(request: CreateOffer): OfferCreated = {
    // create offer entity from request
    val offer = Offer(idGen.create, request.description, request.price, request.expiration)
    repository.create(offer) // add new offer to repository
    OfferCreated(offer.id) // and return success
  }

  /**
    * Lists all offers.
    *
    * @param all if true expired offers are also included
    * @return list of offers
    */
  def list(all: Boolean): OfferInfoList = {
    val now = time.now // use same 'now' for all offers
    val list = repository.list() // just ids
      .flatMap { repository.read } // expand to full objects
      .map { toOfferInfo(_, now) } // convert to transfer objects
      .filter { o => all || !o.isExpired } // filter (if needed)
    OfferInfoList(list.toList)
  }

  /**
    * Gets an offer with given id.
    *
    * @param id offer identifier
    * @return offer
    */
  def get(id: UUID): OfferInfo = {
    val now = time.now
    repository.read(id) // find offer
      .map { toOfferInfo(_, now) } // convert to transfer object
      .getOrElse(throw new OfferNotFound(id)) // ...or fail if not found
  }

  /**
    * Cancels an offer.
    *
    * @param id offer identifier
    * @return offer
    */
  def cancel(id: UUID): OfferInfo = {
    val now = time.now
    repository.read(id) // read current state
      .map { o => Offer(o.id, o.description, o.price, Some(now)) } // clone-with-update
      .flatMap { o => repository.update(o); repository.read(o.id) } // read-after-write
      .map { toOfferInfo(_, now) } // convert to transfer
      .getOrElse(throw new OfferNotFound(id)) // ...or fail if not found
  }
}
