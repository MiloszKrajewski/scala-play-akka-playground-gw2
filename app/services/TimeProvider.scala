package services

import org.joda.time.{DateTime, DateTimeZone}

/**
  * Time provider allowing to use virtual time for testing.
  */
trait TimeProvider {
  /**
    * Current time
    *
    * @return current time
    */
  def now: DateTime
}

/**
  * Default time provider using system's default clock.
  */
class DefaultTimeProvider extends TimeProvider {
  /**
    * Current time
    *
    * @return current time
    */
  override def now: DateTime = DateTime.now(DateTimeZone.UTC)
}
