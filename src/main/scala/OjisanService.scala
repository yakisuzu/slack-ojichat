import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackUser
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

import scala.util.Random

class OjisanService(val repo: OjisanRepository) {
  private val rand: Random = new Random()

  def mentionedMessage(makeMessage: (SlackUser, SlackMessagePosted) => String): Unit =
    repo.onMessage { message =>
      if (repo.hasOjisanMention(message)) {
        repo.sendMessage(
          message.getChannel,
          makeMessage(message.getSender, message),
        ).unsafeRunSync()
        // FIXME メッセージ送信時刻の保持
        IO(())
      } else IO(())
    }.unsafeRunSync()

  def kimagureReaction(): Unit =
    repo.onMessage { message =>
      (repo.isOjiTalk(message), rand.nextInt(100)) match {
        case (ok, _) if ok => IO(()) // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => repo.addReactionToMessage(choiceEmoji(), message)
        case _ => IO(())
      }
    }.unsafeRunSync()

  def choiceEmoji(): String = {
    val i = rand.nextInt(repo.emojis.size)
    repo.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): Unit =
    repo.onMessage { message =>
      for {
        _ <- debug(message.getSender)
        _ <- debug(message)
      } yield ()
    }.unsafeRunSync()

  def debug(u: SlackUser): IO[Unit] = IO {
    println(s"{ userId: ${u.getId}, userName: ${u.getUserName}, realName: ${u.getRealName}, userTitle: ${u.getUserTitle} }")
  }

  def debug(m: SlackMessagePosted): IO[Unit] = IO {
    println(s"{ ts: ${m.getTimestamp}, channelId: ${m.getChannel.getId}, channelName: ${m.getChannel.getName}, messageContent: ${m.getMessageContent} }")
  }
}

object OjisanService {
  def apply(ojisanToken: String): OjisanService =
    new OjisanService(OjisanRepository(ojisanToken))
}
