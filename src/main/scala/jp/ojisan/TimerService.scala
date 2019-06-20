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

  def calcReservationTime(scheduleTime: LocalTime): IO[Option[FiniteDuration]] = {
    val now = LocalTime.now.withSecond(0)
    scheduleTime.compareTo(now) match {
      case c if c <= 0 => IO(None)
      case _ =>
        IO {
          val diffTime = scheduleTime
            .minusHours(now.getHour.toLong)
            .minusMinutes(now.getMinute.toLong)
          val diffSeconds = (diffTime.getHour.hours.toSeconds + diffTime.getMinute.minute.toSeconds).seconds
          Some(diffSeconds)
        }
    }
  }
}
