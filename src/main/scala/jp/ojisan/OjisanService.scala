package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

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
  def apply()(
      implicit
      _repository: OjisanRepository,
      _ojichat: OjichatService
  ): OjisanService =
    new OjisanService {
      override implicit val repository: OjisanRepository = _repository
      override implicit val ojichat: OjichatService      = _ojichat
    }
}
