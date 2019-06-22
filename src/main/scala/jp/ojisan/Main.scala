package jp.ojisan

import java.util.concurrent.{Executors, ScheduledExecutorService}

import cats.data.EitherT
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

object Main extends IOApp with LazyLogging {
  type F[A] = EitherT[IO, Throwable, A]
  val F: ConcurrentEffect[F]                = implicitly[ConcurrentEffect[F]]
  implicit val ec: ExecutionContext         = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  implicit val sc: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

  lazy val conf: Config        = ConfigFactory.load()
  lazy val ojisanName: String  = conf.getString("app.name")
  lazy val ojisanToken: String = conf.getString("app.slackToken")

  val ojisanService  = OjisanService(ojisanToken)
  val ojichatService = OjichatService()

  def run(args: List[String]): IO[ExitCode] =
    F.toIO {
      EitherT
        .right(
          for {
            // debug
            _ <- ojisanService.debugMessage()

            // オジサンはかまってくれると嬉しい
            _ <- ojisanService.mentionedMessage { user: UserValue =>
              ojichatService.getTalk(Some(user.realName)).unsafeRunSync()
            }

            // オジサンはかまいたい
            _ <- ojisanService.kimagureReaction()

            // オジサンはやさしい
            _ <- ojisanService.mentionRequest { at: String =>
              ojichatService.getTalk(Some(at)).unsafeRunSync()
            }

            _ <- IO(logger.info("オジサン準備終わったヨ"))
          } yield ()
        )
        .map(_ => ExitCode.Success)
    }
}
