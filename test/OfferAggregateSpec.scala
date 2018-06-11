import java.util.UUID

import domain.{Money, OfferNotFound, OfferAggregate}
import models.CreateOffer
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import services.{DefaultUuidGenerator, InMemoryOfferRepository}

class OfferAggregateSpec extends PlaySpec {
  val time = new TestTimeProvider(DateTime.now)

  def uuid: UUID = UUID.randomUUID()

  private def createAggregate(): OfferAggregate = {
    val idGen = new DefaultUuidGenerator()
    val repo = new InMemoryOfferRepository()
    new OfferAggregate(idGen, repo, time)
  }

  "OfferAggregate" should {
    "return empty list when initialized" in {
      val aggregate = createAggregate()
      val list = aggregate.list(true)
      list.offers.length must be(0)
    }
  }

  "return id of newly created offer" in {
    val aggregate = createAggregate()
    val command = CreateOffer("offer", Money(10, "GPB"), None)
    val created = aggregate.create(command)
    created.id must not be null
  }

  "add offer to repository" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    val found = aggregate.get(created.id)

    found.id must equal(created.id)
    found.description must equal(command.description)
    found.price must equal(command.price)
    found.expiration must equal(command.expiration)
  }

  "properly resolve expired flag" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    val found = aggregate.get(created.id)

    found.id must equal(created.id)
    found.isExpired must equal(false)
  }

  "properly resolve expired flag, when offer auto-expires" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    time.now = time.now.plusDays(2)
    val found = aggregate.get(created.id)

    found.id must equal(created.id)
    found.isExpired must equal(true)
  }

  "include expired offers when needed" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    time.now = time.now.plusDays(2)

    val list = aggregate.list(true)
    list.offers.length must be(1)
    list.offers.map { _.id } must contain(created.id)
  }

  "not include expired offers when not needed" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    time.now = time.now.plusDays(2)

    val list = aggregate.list(false)
    created.id must not be null
    list.offers.length must be(0)
  }

  "allow to manually cancel offer" in {
    val aggregate = createAggregate()
    val command = CreateOffer("test offer", Money(123, "GPB"), Some(time.now.plusDays(1)))
    val created = aggregate.create(command)
    val cancelled = aggregate.cancel(created.id)

    cancelled.id must equal(created.id)
    cancelled.isExpired must equal(true)

    val found = aggregate.get(created.id)

    found.id must equal(created.id)
    found.isExpired must equal(true)
  }

  "returns error when offer is not found" in {
    val aggregate = createAggregate()
    a[OfferNotFound] must be thrownBy { aggregate.get(uuid) }
  }

  "returns error when trying to cancel non existing offer" in {
    val aggregate = createAggregate()
    a[OfferNotFound] must be thrownBy { aggregate.cancel(uuid) }
  }
}
