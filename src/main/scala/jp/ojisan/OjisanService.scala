package jp.ojisan

import java.util.concurrent.ScheduledExecutorService

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.Random

trait OjisanService extends LazyLogging {
  val repo: OjisanRepository
  val messageContentReservation: MessageContentReservationTimeService = MessageContentReservationTimeService()
  val messageContentUser: MessageContentUserService                   = MessageContentUserService()

  private val rand: Random = new Random()
  def rand100: Int         = rand nextInt 100
  def randN(n: Int): Int   = rand nextInt n

  def mentionedMessage(makeMessage: UserValue => String): IO[Unit] =
    repo.onMessage { message =>
      message match {
        case _ if !(message hasMention repo.ojisan) => IO.unit // オジサンあてじゃない
        case _                                      =>
          // FIXME メッセージ送信時刻の保持
          repo
            .sendMessage(message.channel, makeMessage(message.sender))
            .map(_ => ())
      }
    }

  def kimagureReaction(): IO[Unit] =
    repo.onMessage { message =>
      (message talkedBy repo.ojisan, rand100) match {
        case (ok, _) if ok    => IO.unit // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => IO.unit // 気まぐれで反応しない
        case _                => repo.addReactionToMessage(message.channel, message.timestamp, choiceEmoji())
      }
    }

  def mentionRequest(ojiTalk: String => String)(implicit ec: ExecutionContext, sc: ScheduledExecutorService): IO[Unit] = {
    val wait = WaitService()(ec, sc)
    repo.onMessage { message =>
      (messageContentUser.contentToUsers(message), messageContentReservation.contentToReservationTime(message)) match {
        case _ if !(message hasMention repo.ojisan)             => IO.unit // オジサンあてじゃない
        case (users, _) if repo.filterOjisanIgai(users).isEmpty => IO.unit // 誰にもメンションがない
        case (_, None)                                          => IO.unit // 時間指定ない
        case (users, Some(reservationTime)) =>
          reservationTime.calcRemainingSeconds().flatMap {
            case None =>
              repo
                .sendMessage(message.channel, s"${reservationTime.reservationTime} は過ぎてるよ〜")
                .map(_ => ())
            case Some(remainingSeconds) =>
              for {
                _ <- repo.sendMessage(message.channel, s"${reservationTime.reservationTime} になったら教えるネ")
                _ <- wait.sleepAndRunAsync(remainingSeconds) {
                  for {
                    contentUserIds <- IO(repo.filterOjisanIgai(users).map(_.contentUserId).mkString(" "))
                    _              <- repo.sendMessage(message.channel, ojiTalk(contentUserIds))
                  } yield ()
                }
              } yield ()
          }
      }
    }
  }

  def choiceEmoji(): String = {
    val i = randN(repo.emojis.size)
    repo.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): IO[Unit] =
    repo.onMessage { message =>
      debug(message)
    }

  def debug(m: MessageValue): IO[Unit] = IO {
    logger.debug(
      s"{ ts: ${m.timestamp}, channelId: ${m.channel.getId}, channelName: ${m.channel.getName}, senderUserId: ${m.sender.id}, senderUserRealName: ${m.sender.realName}, content: ${m.content} }"
    )
  }
}

object OjisanService {
  def apply(ojisanToken: String): OjisanService =
    new OjisanService {
      override val repo: OjisanRepository = OjisanRepository(ojisanToken)
    }
}
