package services

import java.util.UUID

import domain.{Offer, OfferAlreadyExists, OfferNotFound}

trait OfferRepository {
  /**
    * Lists all identifiers of all offers in repository.
    *
    * @return list of identifiers
    */
  def list(): Seq[UUID]

  /**
    * Adds offer to repository.
    *
    * @param offer offer to be added
    * @throws OfferAlreadyExists if id already exists
    */
  def create(offer: Offer)

  /**
    * Gets offer from repository.
    *
    * @param id offer identifier
    * @return Some(Offer) if offer was found or None
    */
  def read(id: UUID): Option[Offer]

  /**
    * Updates offer is repository.
    *
    * @param offer offer to be updated
    * @throws OfferNotFound if id could not be found
    */
  def update(offer: Offer)
}
