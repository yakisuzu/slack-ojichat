package jp.ojisan

import java.time.LocalTime

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageContentReservationServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("contentToReservationTime") {
    it("予約時間が取れる") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = null,
        content = "<@dare> <@dore>　oh yeah 01:30",
        threadTs = None
      )
      val actual = MessageContentReservationService().contentToReservationTime(m)
      actual should be(Some(ReservationTimeValue(LocalTime.of(1, 30))))
    }
  }

  describe("contextToReservationUsers") {
    it("メンションが取れる") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = null,
        content = "<@ojisanId> <@dare> <@dore>　oh yeah",
        threadTs = None
      )
      val actual = MessageContentReservationService().contextToReservationUsers(m, UserValue("ojisanId", ""))
      actual should be(Seq(UserValue("dare", ""), UserValue("dore", "")))
    }
  }
}
