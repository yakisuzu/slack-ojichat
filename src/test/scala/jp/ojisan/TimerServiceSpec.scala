package jp.ojisan

import java.time.LocalTime

import cats.effect.IO
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

import scala.concurrent.duration._

class TimerServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("calcRemainingSeconds") {
    it("あってる") {
      val reservationTime = LocalTime.of(10, 31)
      val now             = LocalTime.of(10, 30, 30)
      val df              = TimerService.calcRemainingSeconds(reservationTime, IO(now)).unsafeRunSync()
      df match {
        case None    => throw new AssertionError("こないで〜〜")
        case Some(t) => t should be(30.seconds)
      }
    }
  }
}
