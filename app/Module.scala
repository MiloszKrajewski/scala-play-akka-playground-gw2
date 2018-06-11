import com.google.inject.AbstractModule
import domain.OfferAggregate
import play.libs.akka.AkkaGuiceSupport
import services._

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[UuidGenerator]).to(classOf[DefaultUuidGenerator]).asEagerSingleton()
    bind(classOf[TimeProvider]).to(classOf[DefaultTimeProvider]).asEagerSingleton()
    bind(classOf[OfferRepository]).to(classOf[InMemoryOfferRepository]).asEagerSingleton()
    bind(classOf[OfferAggregate]).asEagerSingleton()
    bindActor(classOf[OfferActor], "offers")
  }
}
