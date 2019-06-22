package jp.ojisan

import java.time.LocalTime

import cats.effect._

import scala.concurrent.duration._

case class ReservationTimeValue(reservationTime: LocalTime) {
  def calcRemainingSeconds(currentTime: IO[LocalTime] = IO(LocalTime.now)): IO[Option[FiniteDuration]] =
    for {
      rTime <- IO(reservationTime)
      now   <- currentTime
      time <- rTime.compareTo(now) match {
        case c if c <= 0 => IO(None)
        case _ =>
          val remainingTime = reservationTime
            .minusHours(now.getHour.toLong)
            .minusMinutes(now.getMinute.toLong)
            .minusSeconds(now.getSecond.toLong)
          val remainingSeconds = (
            remainingTime.getHour.hours.toSeconds
              + remainingTime.getMinute.minutes.toSeconds
              + remainingTime.getSecond.seconds.toSeconds
          ).seconds
          IO(Some(remainingSeconds))
      }
    } yield time
}
