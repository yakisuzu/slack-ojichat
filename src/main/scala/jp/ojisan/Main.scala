package jp.ojisan

import java.util.concurrent.{Executors, ScheduledExecutorService}

import cats.data.EitherT
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

object Main extends IOApp with LazyLogging {
  type F[A] = EitherT[IO, Throwable, A]
  val F: ConcurrentEffect[F] = implicitly[ConcurrentEffect[F]]

  lazy val conf: Config        = ConfigFactory.load()
  lazy val ojisanName: String  = conf.getString("app.name")
  lazy val ojisanToken: String = conf.getString("app.slackToken")

  implicit val ojichatService: OjichatService     = OjichatService()
  implicit val ojisanRepository: OjisanRepository = OjisanRepository(ojisanToken)
  implicit val ec: ExecutionContext               = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  implicit val sc: ScheduledExecutorService       = Executors.newSingleThreadScheduledExecutor()

  val ojisanService: OjisanService =
    OjisanService()(ojisanRepository, ojichatService)
  val ojisanMentionRequestService: OjisanMentionRequestService =
    OjisanMentionRequestService()(ojisanRepository, ojichatService, ec, sc)

  def run(args: List[String]): IO[ExitCode] =
    F.toIO {
      EitherT
        .right(
          for {
            // debug
            _ <- ojisanService.debugMessage()

            // オジサンはかまってくれると嬉しい
            _ <- ojisanService.mentionedMessage()

            // オジサンはかまいたい
            _ <- ojisanService.kimagureReaction()

            // オジサンはやさしい
            _ <- ojisanMentionRequestService.mentionRequest()

            _ <- IO(logger.info("オジサン準備終わったヨ"))
          } yield ExitCode.Success
        )
    }
}
