package jp.ojisan

import java.time.LocalTime
import java.util.concurrent.ScheduledExecutorService

import cats.effect._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait TimerService extends Timer[IO] with LazyLogging {
  val ec: ExecutionContext
  val sc: ScheduledExecutorService

  override val clock: Clock[IO] = new Clock[IO] {
    override def realTime(unit: TimeUnit): IO[Long] = IO {
      unit.convert(System.currentTimeMillis(), MILLISECONDS)
    }
    override def monotonic(unit: TimeUnit): IO[Long] = IO {
      unit.convert(System.nanoTime(), NANOSECONDS)
    }
  }

  override def sleep(timespan: FiniteDuration): IO[Unit] = Async[IO].async { cb =>
    sc.schedule(
      new Runnable {
        def run(): Unit = ec.execute(() => cb(Right(())))
      },
      timespan.length,
      timespan.unit
    )
    ()
  }

  def sleepIO(timespan: FiniteDuration)(f: IO[Unit]): IO[Unit] =
    Effect[IO].runAsync { sleep(timespan) } {
      case Right(_) => f
      case Left(_)  => IO.unit
    }.toIO
}

object TimerService {
  def apply()(implicit _ec: ExecutionContext, _sc: ScheduledExecutorService): TimerService =
    new TimerService {
      override val ec: ExecutionContext         = _ec
      override val sc: ScheduledExecutorService = _sc
    }

  def calcRemainingSeconds(reservationTime: LocalTime): IO[Option[FiniteDuration]] =
    calcRemainingSeconds(reservationTime, IO(LocalTime.now))

  def calcRemainingSeconds(reservationTime: LocalTime, currentTime: IO[LocalTime]): IO[Option[FiniteDuration]] =
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
