package jp.ojisan

import java.util.concurrent.ScheduledExecutorService

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackUser

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

trait OjisanService extends LazyLogging {
  val repo: OjisanRepository

  private val rand: Random = new Random()
  def rand100: Int         = rand nextInt 100
  def randN(n: Int): Int   = rand nextInt n

  def mentionedMessage(makeMessage: SlackUser => String): IO[Unit] =
    repo.onMessage { message =>
      message match {
        case _ if !(message hasMention repo.ojisanId) => IO.unit // オジサンあてじゃない
        case _                                        =>
          // FIXME メッセージ送信時刻の保持
          repo
            .sendMessage(message.channel, makeMessage(message.sender))
            .map(_ => ())
      }
    }

  def kimagureReaction(): IO[Unit] =
    repo.onMessage { message =>
      (message isTalk repo.ojisanId, rand100) match {
        case (ok, _) if ok    => IO.unit // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => IO.unit // 気まぐれで反応しない
        case _                => repo.addReactionToMessage(message.channel, message.ts, choiceEmoji())
      }
    }

  def mentionRequest(ojiTalk: String => String)(implicit ec: ExecutionContext, sc: ScheduledExecutorService): IO[Unit] =
    repo.onMessage { message =>
      (message.contextToUserIds, message.contextToTime) match {
        case _ if !(message hasMention repo.ojisanId)                 => IO.unit // オジサンあてじゃない
        case (userIds, _) if repo.filterOtherUserIds(userIds).isEmpty => IO.unit // 誰にもメンションがない
        case (_, None)                                                => IO.unit // 時間指定ない
        case (userIds, Some(time)) =>
          for {
            _ <- repo.sendMessage(message.channel, s"$time になったら教えるネ")
            _ <- for {
              // TODO 予定時刻 - 現在時刻 = sleep
              _            <- TimerService()(ec, sc).sleep(5.seconds)
              contextUsers <- IO(repo.filterOtherUserIds(userIds).map(MessageEntity.toContextUserId).mkString(" "))
              _            <- repo.sendMessage(message.channel, ojiTalk(contextUsers))
            } yield ()
          } yield ()
      }
    }

  def choiceEmoji(): String = {
    val i = randN(repo.emojis.size)
    repo.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): IO[Unit] =
    repo.onMessage { message =>
      for {
        _ <- debug(message.sender)
        _ <- debug(message)
      } yield ()
    }

  def debug(u: SlackUser): IO[Unit] = IO {
    logger.debug(
      s"{ userId: ${u.getId}, userName: ${u.getUserName}, realName: ${u.getRealName}, userTitle: ${u.getUserTitle} }"
    )
  }

  def debug(m: MessageEntity): IO[Unit] = IO {
    logger.debug(
      s"{ ts: ${m.ts}, channelId: ${m.channel.getId}, channelName: ${m.channel.getName}, content: ${m.context} }"
    )
  }
}

object OjisanService {
  def apply(ojisanToken: String): OjisanService =
    new OjisanService {
      override val repo: OjisanRepository = OjisanRepository(ojisanToken)
    }
}
