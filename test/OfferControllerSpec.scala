import java.util.UUID

import domain.Money
import models.Formatters._
import models.{CreateOffer, OfferInfo}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class OfferControllerSpec extends PlaySpec with GuiceOneAppPerSuite {
  def uuid: UUID = UUID.randomUUID()

  "OffersController" should {
    "return empty list with empty repository" in {
      val list = route(app, FakeRequest(GET, "/api/offer")).get

      status(list) must be(Status.OK)
      contentType(list).get must equal("application/json")
      contentAsJson(list) must equal(Json.arr())
    }

    "handle NotFoundException with NotFound result" in {
      val found = route(app, FakeRequest(GET, s"/api/offer/$uuid")).get

      status(found) must be(Status.NOT_FOUND)
    }

    "create new offers" in {
      val command = CreateOffer("description", Money(10, "GBP"), None)
      val created = route(app, FakeRequest(POST, "/api/offer").withJsonBody(Json.toJson(command))).get

      status(created) must be(Status.CREATED)
      contentType(created).get must equal("application/json")
    }

    "return created offers" in {
      val command = CreateOffer("description", Money(10, "GBP"), None)
      val created = route(app, FakeRequest(POST, "/api/offer").withJsonBody(Json.toJson(command))).get
      val id = contentAsJson(created).validate[UUID].get
      val found = route(app, FakeRequest(GET, s"/api/offer/$id")).get
      val expected = OfferInfo(id, command.description, Money(10, "GBP"), None, isExpired = false)

      status(found) must be(Status.OK)
      contentType(found).get must be("application/json")
      contentAsJson(found).validate[OfferInfo].get must equal(expected)
    }

    "cancels offers" in {
      val command = CreateOffer("description", Money(10, "GBP"), None)
      val created = route(app, FakeRequest(POST, "/api/offer").withJsonBody(Json.toJson(command))).get
      val id = contentAsJson(created).validate[UUID].get
      val cancelled = route(app, FakeRequest(PATCH, s"/api/offer/$id/cancel")).get

      status(cancelled) must be(Status.OK)
      contentType(cancelled).get must be("application/json")
      val offer = contentAsJson(cancelled).validate[OfferInfo].get
      offer.expiration must not be None
      offer.isExpired must be(true)
    }

    "fails to cancel non existing offer" in {
      val cancelled = route(app, FakeRequest(PATCH, s"/api/offer/$uuid/cancel")).get

      status(cancelled) must be(Status.NOT_FOUND)
    }
  }
}
