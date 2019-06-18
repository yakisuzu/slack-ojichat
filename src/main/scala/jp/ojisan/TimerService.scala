package jp.ojisan

import java.util.concurrent.ScheduledExecutorService

import cats.effect._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait TimerService extends Timer[IO] {
  val ec: ExecutionContext
  val sc: ScheduledExecutorService

  override val clock: Clock[IO] = new Clock[IO] {
    // def realTimeSeconds(): IO[Long] = realTime(SECONDS)
    override def realTime(unit: TimeUnit): IO[Long] = IO {
      unit.convert(System.currentTimeMillis(), MILLISECONDS)
    }
    override def monotonic(unit: TimeUnit): IO[Long] = IO {
      unit.convert(System.nanoTime(), NANOSECONDS)
    }
  }

  override def sleep(timespan: FiniteDuration): IO[Unit] = IO.cancelable { cb =>
    IO {
      sc.schedule(
          new Runnable {
            def run(): Unit = ec.execute(() => cb(Right(())))
          },
          timespan.length,
          timespan.unit
        )
        .cancel(false)
    }.map(_ => ())
  }

  def sleepSync(timespan: FiniteDuration)(f: IO[Unit]): SyncIO[Unit] = Effect[IO].runAsync { sleep(timespan) } {
    case Right(_) => f
    case Left(_)  => IO.unit
  }
}

object TimerService {
  def apply()(implicit _ec: ExecutionContext, _sc: ScheduledExecutorService): TimerService =
    new TimerService {
      override val ec: ExecutionContext         = _ec
      override val sc: ScheduledExecutorService = _sc
    }
}
