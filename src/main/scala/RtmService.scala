import cats.effect.IO
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackMessageHandle, SlackSession, SlackUser}

import scala.collection.JavaConverters._
import scala.util.Random

class RtmService(val session: SlackSession) {
  session.connect()

  private val rand: Random = new Random()
  private lazy val ojisanId: String = session.sessionPersona().getId
  private lazy val emojis: Set[String] = session.listEmoji().getReply.getEmojis.keySet().asScala.toSet

  def getUser(userId: String): Option[SlackUser] =
    Option(session.findUserById(userId))

  def addReactionToMessage(emoji: String, m: SlackMessagePosted): IO[Unit] = IO {
    session.addReactionToMessage(m.getChannel, m.getTimestamp, emoji)
    ()
  }

  def onMessage(cb: (SlackMessagePosted, SlackSession) => IO[Unit]): IO[Unit] = IO {
    session.addMessagePostedListener { (event, s) =>
      cb(event, s).unsafeRunSync()
    }
  }

  def onMessage(cb: SlackMessagePosted => IO[Unit]): IO[Unit] =
    onMessage { (event, _) =>
      cb(event)
    }

  //  def onMessage(): IO[SlackMessagePosted] = Async[IO].async { cb =>
  //    session.addMessagePostedListener {
  //      (event: SlackMessagePosted, _) => cb(Right(event)).unsafeRunSync()
  //    }
  //  }

  def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageHandle[SlackMessageReply]] = IO {
    session.sendMessage(channel, m)
  }

  def helloOjisan(): Unit = IO {
    if (session.isConnected) {
      sendMessage(
        session.findChannelByName("random"),
        "よ〜〜〜し、オジサンがんばっちゃうゾ",
      )
      ()
    } else {
      Thread.sleep(1000)
      helloOjisan()
    }
  }.unsafeRunSync()

  def mentionedMessage(makeMessage: (SlackUser, SlackMessagePosted) => String): Unit =
    onMessage { message =>
      if (message.getMessageContent.contains(ojisanId)) {
        sendMessage(
          message.getChannel,
          makeMessage(message.getSender, message),
        ).unsafeRunSync()
        // FIXME メッセージ送信時刻の保持
        IO(())
      } else IO(())
    }.unsafeRunSync()

  def kimagureReaction(): Unit =
    onMessage { message =>
      (message.getSender.getId, rand.nextInt(100)) match {
        case (`ojisanId`, _) => IO(()) // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => addReactionToMessage(choiceEmoji(), message)
        case _ => IO(())
      }
    }.unsafeRunSync()

  def choiceEmoji(): String = {
    val i = rand.nextInt(emojis.size)
    emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): Unit =
    onMessage { message =>
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

object RtmService {
  def apply(ojisanToken: String): RtmService =
    new RtmService(SlackSessionFactory.createWebSocketSlackSession(ojisanToken))
}
