package jp.ojisan

import java.time.LocalTime
import java.util.concurrent.ScheduledExecutorService

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackChannel

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait OjisanMentionRequestService extends OjisanService with LazyLogging {
  implicit val ec: ExecutionContext
  implicit val sc: ScheduledExecutorService

  // lazyにしないと動かないのまじわからん
  private lazy val waitService = WaitService()(ec, sc)

  def mentionRequest(): IO[Unit] =
    repository.onMessage(onMessageAction)

  def onMessageAction(message: MessageValue): IO[Unit] = {
    val hasNotMention = !(message hasMention repository.ojisan)
    if (hasNotMention) {
      // オジサンあてじゃない
      return IO.unit
    }

    val reservationUsers = contextToReservationUsers(message)
    if (reservationUsers.isEmpty) {
      // 誰にもメンションがない
      return IO.unit
    }

    val someReservationTime = messageContentReservation.contentToReservationTime(message)
    someReservationTime match {
      case None => IO.unit // 時間指定ない
      case Some(reservationTime) =>
        reservationTime.calcRemainingSeconds().flatMap {
          case None =>
            reservationTimePassed(
              message.channel,
              reservationTime.reservationTime
            )
          case Some(remainingSeconds) =>
            reservationSendMessage(
              message.channel,
              reservationTime.reservationTime,
              remainingSeconds,
              reservationUsers
            )
        }
    }
  }

  def contextToReservationUsers(m: MessageValue): Seq[UserValue] = {
    val mentionUsers = messageContentUser.contentToUsers(m)
    repository.filterOjisanIgai(mentionUsers)
  }

  def reservationTimePassed(c: SlackChannel, t: LocalTime): IO[Unit] =
    repository
      .sendMessage(c, s"$t は過ぎてるよ〜")
      .map(_ => ())

  def reservationSendMessage(
      c: SlackChannel,
      t: LocalTime,
      remainingSeconds: FiniteDuration,
      reservationUsers: Seq[UserValue]
  ): IO[Unit] =
    for {
      _ <- repository.sendMessage(c, s"$t になったら教えるネ")
      _ <- IO(logger.debug(s"reservation after $remainingSeconds"))
      _ <- waitService.sleepAndRunAsync(remainingSeconds) {
        for {
          contentUserIds <- IO(reservationUsers.map(_.contentUserId).mkString(" "))
          ojiTalk        <- ojichat.getTalk(Some(contentUserIds))
          _              <- repository.sendMessage(c, ojiTalk)
        } yield ()
      }
    } yield ()
}

object OjisanMentionRequestService {
  def apply()(
      implicit
      _repository: OjisanRepository,
      _ojichat: OjichatService,
      _ec: ExecutionContext,
      _sc: ScheduledExecutorService
  ): OjisanMentionRequestService =
    new OjisanMentionRequestService {
      override implicit val repository: OjisanRepository = _repository
      override implicit val ojichat: OjichatService      = _ojichat
      override implicit val ec: ExecutionContext         = _ec
      override implicit val sc: ScheduledExecutorService = _sc
    }
}
