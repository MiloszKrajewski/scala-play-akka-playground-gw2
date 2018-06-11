package services

import java.util.UUID

/**
  * Guid provider.
  */
trait UuidGenerator {
  /**
    * Return new guid.
    *
    * @return mew guid.
    */
  def create: UUID
}

/**
  * Default guid generator using UUID class.
  */
class DefaultUuidGenerator extends UuidGenerator {
  /**
    * Return new guid.
    *
    * @return mew guid.
    */
  override def create: UUID = UUID.randomUUID()
}
