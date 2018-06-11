package services

import akka.actor.Actor
import domain.OfferAggregate
import javax.inject.Inject
import models.{CancelOffer, CreateOffer, GetOffer, GetOffers}
import play.api.Logger

class OfferActor @Inject()(aggregate: OfferAggregate) extends Actor {
  private def respond[Response](action: => Response): Unit = {
    // response is either result or exception
    val response = try {action} catch {case e: Throwable => e}
    context.sender() ! response // at it sent back to sender
  }

  override def receive: Receive = {
    case r: CreateOffer => respond(aggregate.create(r))
    case r: GetOffers => respond(aggregate.list(r.all))
    case r: GetOffer => respond(aggregate.get(r.id))
    case r: CancelOffer => respond(aggregate.cancel(r.id))
    case m => Logger.warn(s"Unhandled message type: ${m.getClass.getName}")
  }
}
