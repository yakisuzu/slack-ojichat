package jp.ojisan

import cats.effect.{Async, IO}
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession}

import scala.collection.JavaConverters._

trait OjisanRepository extends LazyLogging {
  val session: SlackSession
  lazy val ojisan: UserValue   = UserValue(session.sessionPersona())
  lazy val emojis: Set[String] = session.listEmoji().getReply.getEmojis.keySet().asScala.toSet

  def findUser(user: UserValue): Option[UserValue] =
    Option(session.findUserById(user.id)).map(UserValue(_))

  def filterOjisanIgai(users: Seq[UserValue]): Seq[UserValue] =
    users.filter(_ != ojisan)

  def addReactionToMessage(channel: SlackChannel, ts: String, emoji: String): IO[Unit] =
    IO {
      session.addReactionToMessage(channel, ts, emoji)
    }.map(_ => ())

  def onMessage(cb: MessageValue => IO[Unit]): IO[Unit] = IO {
    session.addMessagePostedListener { (event, _) =>
      cb(MessageValue(event)) unsafeRunSync
    }
  }

  def onMessageAsync(): IO[MessageValue] = Async[IO].async { cb =>
    session.addMessagePostedListener { (event, _) =>
      cb(Right(MessageValue(event)))
    }
  }

  def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageReply] =
    IO {
      session.sendMessage(channel, m).getReply
    }

  def helloOjisan(): IO[Unit] =
    IO {
      sendMessage(
        session.findChannelByName("random"),
        "よ〜〜〜し、オジサンがんばっちゃうゾ"
      )
    }.map(_ => ())
}

object OjisanRepository {
  def apply(ojisanToken: String): OjisanRepository =
    new OjisanRepository {
      override val session: SlackSession =
        SlackSessionFactory.createWebSocketSlackSession(ojisanToken) match {
          case s =>
            s.connect()
            s
        }
    }
}
