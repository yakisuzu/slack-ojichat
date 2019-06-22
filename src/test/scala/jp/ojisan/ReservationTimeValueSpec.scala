package jp.ojisan

import java.time.LocalTime

import cats.effect.IO
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.concurrent.duration._

class ReservationTimeValueSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("calcRemainingSeconds") {
    it("残り時間あってる") {
      val reservationTime = LocalTime.of(10, 31)
      val now             = LocalTime.of(10, 30, 30)
      val df              = ReservationTimeValue(reservationTime).calcRemainingSeconds(IO(now)).unsafeRunSync()
      df match {
        case None    => throw new AssertionError("こないで〜〜")
        case Some(t) => t should be(30.seconds)
      }
    }
  }
}
