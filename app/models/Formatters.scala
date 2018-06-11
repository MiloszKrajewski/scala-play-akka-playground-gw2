package models

import java.util.UUID

import domain.Money
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

/**
  * Default JSON formatter for objects in this project.
  */
object Formatters {
  private val timeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val time: Format[DateTime] = Format(
    { j => j.validate[String].map(DateTime.parse) },
    { d => JsString(d.toString(timeFormat)) }
  )

  implicit val uuid: Format[UUID] = Format(
    { j => j.validate[String].map(UUID.fromString) },
    { g => JsString(g.toString) }
  )

  implicit val money: Format[Money] = Json.format[Money]

  implicit val createOffer: Format[CreateOffer] = Json.format[CreateOffer]

  implicit val offerInfo: Format[OfferInfo] = Json.format[OfferInfo]
}
