package jp.ojisan

import java.util.concurrent.ScheduledExecutorService

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.Random

trait OjisanService extends LazyLogging {
  implicit val repository: OjisanRepository
  implicit val ojichat: OjichatService

  val messageContentReservation: MessageContentReservationTimeService = MessageContentReservationTimeService()
  val messageContentUser: MessageContentUserService                   = MessageContentUserService()

  private val rand: Random   = new Random()
  def randN(n: Int): IO[Int] = IO(rand nextInt n)

  def mentionedMessage(): IO[Unit] =
    repository.onMessage {
      case message if !(message hasMention repository.ojisan) => IO.unit // オジサンあてじゃない
      case message =>
        for {
          ojiTalk <- ojichat.getTalk(Some(message.sender.realName))
          // FIXME メッセージ送信時刻の保持
          _ <- repository.sendMessage(message.channel, ojiTalk)
        } yield ()
    }

  def kimagureReaction(): IO[Unit] =
    repository.onMessage {
      case message if message talkedBy repository.ojisan => IO.unit // 自分の発言にはリアクションしない
      case message =>
        for {
          ok    <- randN(100).map(_ < 50) // 気まぐれで反応しない
          emoji <- choiceEmoji()
          _ <- if (ok) repository.addReactionToMessage(message.channel, message.timestamp, emoji)
          else IO.unit
        } yield ()
    }

  def mentionRequest()(implicit ec: ExecutionContext, sc: ScheduledExecutorService): IO[Unit] = {
    val wait = WaitService()(ec, sc)
    repository.onMessage { message =>
      (messageContentUser.contentToUsers(message), messageContentReservation.contentToReservationTime(message)) match {
        case _ if !(message hasMention repository.ojisan)             => IO.unit // オジサンあてじゃない
        case (users, _) if repository.filterOjisanIgai(users).isEmpty => IO.unit // 誰にもメンションがない
        case (_, None)                                                => IO.unit // 時間指定ない
        case (users, Some(reservationTime)) =>
          reservationTime.calcRemainingSeconds().flatMap {
            case None =>
              repository
                .sendMessage(message.channel, s"${reservationTime.reservationTime} は過ぎてるよ〜")
                .map(_ => ())
            case Some(remainingSeconds) =>
              for {
                _ <- repository.sendMessage(message.channel, s"${reservationTime.reservationTime} になったら教えるネ")
                _ <- wait.sleepAndRunAsync(remainingSeconds) {
                  for {
                    contentUserIds <- IO(repository.filterOjisanIgai(users).map(_.contentUserId).mkString(" "))
                    ojiTalk        <- ojichat.getTalk(Some(contentUserIds))
                    _              <- repository.sendMessage(message.channel, ojiTalk)
                  } yield ()
                }
              } yield ()
          }
      }
    }
  }

  def choiceEmoji(): IO[String] =
    for {
      i     <- randN(repository.emojis.size)
      emoji <- IO(repository.emojis.zipWithIndex.find(_._2 == i).map(_._1).get)
    } yield emoji

  def debugMessage(): IO[Unit] =
    repository.onMessage { message =>
      debug(message)
    }

  def debug(m: MessageValue): IO[Unit] = IO {
    logger.debug(
      s"{ ts: ${m.timestamp}, channelId: ${m.channel.getId}, channelName: ${m.channel.getName}, senderUserId: ${m.sender.id}, senderUserRealName: ${m.sender.realName}, content: ${m.content} }"
    )
  }
}

object OjisanService {
  def apply(ojisanToken: String)(implicit _ojichat: OjichatService): OjisanService =
    new OjisanService {
      override implicit val repository: OjisanRepository = OjisanRepository(ojisanToken)
      override implicit val ojichat: OjichatService      = _ojichat
    }
}
