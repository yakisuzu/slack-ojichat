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

  lazy val conf: Config                       = ConfigFactory.load()
  lazy val ojisanName: String                 = conf.getString("app.name")
  lazy val ojisanToken: String                = conf.getString("app.slackToken")
  lazy val debugMessageMode: DebugMessageMode = DebugMessageMode(conf.getString("app.debug_message_mode"))

  implicit val ojichat: OjichatService            = OjichatService()
  implicit val ojisanRepository: OjisanRepository = OjisanRepository(ojisanToken)
  implicit val ec: ExecutionContext               = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  implicit val sc: ScheduledExecutorService       = Executors.newSingleThreadScheduledExecutor()

  val ojisanMentionMessage: OjisanMentionMessageService =
    OjisanMentionMessageService(ojisanRepository, ojichat)
  val ojisanKimagureReaction: OjisanKimagureReactionService =
    OjisanKimagureReactionService(ojisanRepository)
  val ojisanKimagureMessage: OjisanKimagureMessageService =
    OjisanKimagureMessageService(ojisanRepository, ojichat)
  val ojisanThreadMessage: OjisanThreadMessageService =
    OjisanThreadMessageService(ojisanRepository, ojichat)

  def run(args: List[String]): IO[ExitCode] = {
    // 無理にIO[Unit]で取ってるけど、それだめで
    // unsafeRunSync時点でEitherT[IO, Throwable, Unit]にしたいんですよ
    val r: IO[Unit] = for {
      // debug
      _ <- debugMessageMode.debugMessage(ojisanRepository)

      // オジサンはかまってくれると嬉しい
      _ <- ojisanMentionMessage.mentionMessage()

      // オジサンはかまいたい
      _ <- ojisanKimagureReaction.kimagureReaction()
      _ <- ojisanKimagureMessage.kimagureMessage()

      // オジサンはスレッドでかまいたい
      _ <- ojisanThreadMessage.alwaysMessage()

      _ <- IO(logger.info("オジサン準備終わったヨ"))
    } yield ()
    EitherT(r.attempt).fold(_ => ExitCode.Error, _ => ExitCode.Success)
  }
}
