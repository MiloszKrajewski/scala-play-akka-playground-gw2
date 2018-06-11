package services

import java.util.UUID

import domain.{Offer, OfferAlreadyExists, OfferNotFound}

import scala.collection.mutable

class InMemoryOfferRepository extends OfferRepository {
  private val storage: mutable.Map[UUID, Offer] = mutable.Map()

  /**
    * Lists all identifiers of all offers in repository.
    *
    * @return list of identifiers
    */
  override def list(): Seq[UUID] = storage.keys.toSeq

  /**
    * Adds offer to repository.
    *
    * @param offer offer to be added
    * @throws OfferAlreadyExists if id already exists
    */
  override def create(offer: Offer): Unit = {
    val id = offer.id
    if (storage.contains(id))
      throw new OfferAlreadyExists(id)
    storage.update(id, offer)
  }

  /**
    * Gets offer from repository.
    *
    * @param id offer identifier
    * @return Some(Offer) if offer was found or None
    */
  override def read(id: UUID): Option[Offer] = storage.get(id)

  /**
    * Updates offer is repository.
    *
    * @param offer offer to be updated
    * @throws OfferNotFound if id could not be found
    */
  override def update(offer: Offer): Unit = {
    val id = offer.id
    if (!storage.contains(id))
      throw new OfferNotFound(id)
    storage.update(id, offer)
  }
}
