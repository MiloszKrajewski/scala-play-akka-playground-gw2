import org.joda.time.DateTime
import services.TimeProvider

class TestTimeProvider(val zero: DateTime) extends TimeProvider {
  private var current = zero
  override def now: DateTime = current
  def now_=(value: DateTime): Unit = current = value
  def reset(): Unit = current = zero
}
