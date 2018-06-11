import java.util.UUID

import domain.{OfferAlreadyExists, Money, OfferNotFound, Offer}
import org.joda.time.DateTime
import org.scalatestplus.play._
import services.InMemoryOfferRepository

class RepositorySpec extends PlaySpec {
  "Repository" should {
    "return empty list when created" in {
      val repo = new InMemoryOfferRepository()

      repo.list().length must equal(0)
    }

    "add new element" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val expiration = DateTime.now
      repo.create(Offer(id, "description", Money(10, "USD"), Some(expiration)))

      repo.list().length must equal(1)
    }

    "return None is offer cannot be found" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()

      repo.read(id).isDefined must equal(false)
    }

    "find freshly added element" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val expiration = DateTime.now
      repo.create(Offer(id, "description", Money(10, "USD"), Some(expiration)))

      repo.read(id).isDefined must equal(true)
    }

    "throw exception on duplicate offers" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val expiration = DateTime.now
      val offer = Offer(id, "description", Money(10, "USD"), Some(expiration))
      repo.create(offer)

      a[OfferAlreadyExists] must be thrownBy { repo.create(offer) }
    }


    "return added offer unchanged" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val expiration = DateTime.now
      val expected = Offer(id, "description", Money(10, "USD"), Some(expiration))
      repo.create(expected)

      repo.read(id).get must equal(expected)
    }

    "fail to update not existing offer" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val updated = Offer(id, "updated", Money(20, "GBP"), None)

      a[OfferNotFound] must be thrownBy { repo.update(updated) }
      repo.read(id).isDefined must equal(false)
    }

    "update offers in place" in {
      val repo = new InMemoryOfferRepository()
      val id = UUID.randomUUID()
      val expiration = DateTime.now
      val expected = Offer(id, "description", Money(10, "USD"), Some(expiration))
      repo.create(expected)

      repo.read(id).get must equal(expected)

      val updated = Offer(id, "updated", Money(20, "GBP"), None)
      repo.update(updated)

      repo.read(id).get must equal(updated)
    }
  }
}
