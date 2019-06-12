package jp.ojisan

import java.time.format.DateTimeFormatter

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackUser

import scala.util.Random

trait OjisanService extends LazyLogging {
  val repo: OjisanRepository
  private val rand: Random = new Random()
  def rand100: Int         = rand nextInt 100
  def randN(n: Int): Int   = rand nextInt n

  def mentionedMessage(makeMessage: SlackUser => String): Unit =
    repo.onMessage { message =>
      if (message hasMention repo.ojisanId) {
        repo
          .sendMessage(
            message.channel,
            makeMessage(message.sender)
          )
          .unsafeRunSync()
        // FIXME メッセージ送信時刻の保持
        IO(())
      } else IO(())
    } unsafeRunSync

  def kimagureReaction(): Unit =
    repo.onMessage { message =>
      (message isTalk repo.ojisanId, rand100) match {
        case (ok, _) if ok => IO(()) // 自分の発言にはリアクションしない
        case (_, n) if n < 50 =>
          repo.addReactionToMessage(message.channel, message.ts, choiceEmoji())
        case _ => IO(())
      }
    } unsafeRunSync

  def mentionRequest(ojiTalk: String => String): Unit =
    repo.onMessage { message =>
      (repo.getOtherUserIds(message.contextToIds), message.contextToTime) match {
        case (ids, _) if ids.isEmpty => IO(())
        case (_, None)               => IO(())
        case (ids, Some(time)) =>
          IO {
            repo
              .sendMessage(
                message.channel,
                s"${time.format(DateTimeFormatter.ofPattern("HH:mm"))} になったら教えるネ"
              )
              .unsafeRunSync
            // FIXME Timer
            repo
              .sendMessage(message.channel, ojiTalk(ids.mkString(" ")))
              .unsafeRunSync
            ()
          }
      }
    } unsafeRunSync

  def choiceEmoji(): String = {
    val i = randN(repo.emojis.size)
    repo.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): Unit =
    repo.onMessage { message =>
      for {
        _ <- debug(message.sender)
        _ <- debug(message)
      } yield ()
    } unsafeRunSync

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
