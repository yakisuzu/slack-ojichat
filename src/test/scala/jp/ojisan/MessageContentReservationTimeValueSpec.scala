package jp.ojisan

import java.time.LocalTime

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageContentReservationTimeValueSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("contentToReservationTime") {
    it("予約時間が取れる") {
      val c = new MessageValue(
        channel = null,
        timestamp = "",
        sender = null,
        content = "<@dare> <@dore>　oh yeah 01:30"
      )
      val actual = MessageContentReservationTimeService().contentToReservationTime(c)
      actual should be(Some(ReservationTimeValue(LocalTime.of(1, 30))))
    }
  }
}
