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
        ojichat
          .getTalk(Some(message.sender.realName))
          .flatMap { ojiTalk =>
            // FIXME メッセージ送信時刻の保持
            repository.sendMessage(message.channel, ojiTalk).map(_ => ())
          }
    }

  def kimagureReaction(): IO[Unit] =
    repository.onMessage {
      case message if message talkedBy repository.ojisan => IO.unit // 自分の発言にはリアクションしない
      case message =>
        randN(100).flatMap {
          case n if n > 50 => IO.unit // 気まぐれで反応しない
          case _ =>
            choiceEmoji().flatMap { emoji =>
              repository.addReactionToMessage(message.channel, message.timestamp, emoji)
            }
        }
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
    randN(repository.emojis.size).map { i =>
      repository.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
    }

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
