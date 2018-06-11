import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import domain.{Money, OfferAggregate, OfferNotFound}
import models._
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import services.{DefaultUuidGenerator, InMemoryOfferRepository, OfferActor}

import scala.concurrent.Await
import scala.concurrent.duration._


class OfferActorSpec extends PlaySpec {
  private implicit val requestTimeout: Timeout = 1.seconds
  val time = new TestTimeProvider(DateTime.now)

  def uuid: UUID = UUID.randomUUID()

  def withActor(func: (Any => Any) => Unit): Unit = {
    time.reset()
    val system = ActorSystem("test")
    val idGen = new DefaultUuidGenerator()
    val repo = new InMemoryOfferRepository()
    val actor = system.actorOf(Props(new OfferActor(new OfferAggregate(idGen, repo, time))))

    def query(request: Any): Any = {
      val response = actor ? request
      Await.ready(response, requestTimeout.duration)
      response.value.get.get
    }

    try {
      func(query)
    } finally {
      system.terminate()
    }
  }

  //  override def receive: Receive = {
  //    case r: GetOffers => respond(aggregate.list(r.all))
  //  }


  "OffersActor" should {
    "handles CreateOffer messages" in {
      withActor { query =>
        val command = CreateOffer("offer", Money(10, "GPB"), None)
        val created = query(command).asInstanceOf[OfferCreated]
        created.id must not be null
      }
    }

    "handles CancelOffer message, when offer exists" in {
      withActor { query =>
        val command = CreateOffer("test offer", Money(123, "GPB"), None)
        val created = query(command).asInstanceOf[OfferCreated]
        val cancelled = query(CancelOffer(created.id)).asInstanceOf[OfferInfo]

        cancelled.id must equal(created.id)
        cancelled.isExpired must equal(true)
      }
    }

    "handles CancelOffer message, when offer does not exists" in {
      withActor { query =>
        val response = query(CancelOffer(uuid))

        response.isInstanceOf[OfferNotFound] must equal(true)
      }
    }

    "handles GetOffer message, when offer exists" in {
      withActor { query =>
        val command = CreateOffer("test offer", Money(123, "GPB"), None)
        val created = query(command).asInstanceOf[OfferCreated]
        val found = query(GetOffer(created.id)).asInstanceOf[OfferInfo]

        found.id must equal(created.id)
      }
    }

    "handles GetOffer message, when offer does not exists" in {
      withActor { query =>
        val response = query(CancelOffer(uuid))

        response.isInstanceOf[OfferNotFound] must equal(true)
      }
    }

    "handles GetOffers" in {
      withActor { query =>
        val list = query(GetOffers(true)).asInstanceOf[OfferInfoList]
        list.offers.length must be(0)
      }
    }
  }
}

