package jp.ojisan

import cats.data.EitherT
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackUser

object Main extends IOApp with LazyLogging {
  type F[A] = EitherT[IO, Throwable, A]
  val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]

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
            _ <- ojisanService.mentionedMessage { user: SlackUser =>
              ojichatService.getTalk(Some(user.getRealName)).unsafeRunSync()
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
