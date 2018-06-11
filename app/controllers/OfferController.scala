package controllers

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import domain.{OfferAlreadyExists, OfferNotFound}
import javax.inject.{Inject, Named, Singleton}
import models.Formatters._
import models._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class OfferController @Inject()(
  cc: ControllerComponents, ec: ExecutionContext, @Named("offers") oa: ActorRef
) extends AbstractController(cc) {

  private implicit val requestTimeout: Timeout = 5.seconds
  private implicit val executionContext: ExecutionContext = ec
  private val offersActor: ActorRef = oa

  Logger.info("OfferController started...")

  /**
    * Common wrapper for actions with JSON body. Body is parsed with 'parse' function, passed to actor,
    * while returned result is adapted to http response with 'resolve' function.
    *
    * @param parse   function parsing body json, usually (_.asOpt[Request])
    * @param resolve function resolving response into proper HTTP responses
    * @tparam Request type of request json
    * @return controller action
    */
  def fromBody[Request](parse: JsValue => Option[Request])(resolve: Any => Result): Action[AnyContent] =
    Action.async { request =>
      request.body.asJson.flatMap(parse) match {
        case None => Future(BadRequest("JSON could not be parsed"))
        case Some(r) => (offersActor ? r).map(resolve)
      }
    }

  /**
    * Common wrapper for actions with no body. Body is assumed JSON which is parsed with 'parse' function,
    * and returned result is adapted to http response with 'resolve' function.
    *
    * @param build   function building command from parameters
    * @param resolve function resolving response into proper HTTP responses
    * @tparam Request type of command built
    * @return action
    */
  def fromUrl[Request](build: => Request)(resolve: Any => Result): Action[AnyContent] =
    Action.async {
      val request = try {Success(build)} catch {case e: Throwable => Failure(e)}
      request match {
        case Failure(e) => Future(BadRequest(e.getMessage))
        case Success(r) => (offersActor ? r).map(resolve)
      }
    }

  /**
    * POST /api/offer
    * Creates new offer. Takes 'CreateOffer' json body and returns offer id as json string.
    *
    * @return id of created offer
    */
  def create(): Action[AnyContent] = fromBody(_.asOpt[CreateOffer]) {
    case r: OfferCreated => Created(Json.toJson(r.id))
    case e: OfferAlreadyExists => Conflict(e.getMessage)
    case e: Exception => InternalServerError(e.getMessage)
  }

  /**
    * GET /api/offer?all=[all:bool]
    * Lists all offers. Can limit result to active offers by using '?all=false' or '?all=0'
    * query parameter. Returns list of offers (full objects).
    *
    * @param all indicates if all (including expired ones) offers should be returned
    * @return list of offers
    */
  def list(all: Option[Boolean]): Action[AnyContent] = fromUrl(GetOffers(all.getOrElse(true))) {
    case r: OfferInfoList => Ok(Json.toJson(r.offers))
    case e: Exception => InternalServerError(e.getMessage)
  }

  /**
    * GET /api/offer/[id:guid]
    * Gets single offer. Offer id is passed as part of URI. Returns full offer information.
    *
    * @param id identifier of an offer
    * @return offer information
    */
  def read(id: UUID): Action[AnyContent] = fromUrl(GetOffer(id)) {
    case r: OfferInfo => Ok(Json.toJson(r))
    case e: OfferNotFound => NotFound(e.getMessage)
    case e: Exception => InternalServerError(e.getMessage)
  }

  /**
    * PATCH /api/offer/[id:bool]/cancel
    * Cancels given offer. Please note that is is implemented as PATCH, not POST (which would be an option as well,
    * but would create expectation that 'cancellations' are a resource and can be accessed separately)
    *
    * @param id identifier of an offer
    * @return offer information after cancellation
    */
  def cancel(id: UUID): Action[AnyContent] = fromUrl(CancelOffer(id)) {
    case r: OfferInfo => Ok(Json.toJson(r))
    case e: OfferNotFound => NotFound(e.getMessage)
    case e: Exception => InternalServerError(e.getMessage)
  }
}
