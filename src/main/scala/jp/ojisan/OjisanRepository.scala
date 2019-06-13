package jp.ojisan

import cats.effect.{Async, IO}
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession, SlackUser}

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}

trait OjisanRepository extends LazyLogging {
  val session: SlackSession
  lazy val ojisanId: String    = session.sessionPersona().getId
  lazy val emojis: Set[String] = session.listEmoji().getReply.getEmojis.keySet().asScala.toSet

  def findUser(userId: String): Option[SlackUser] =
    Option(session.findUserById(userId))

  def filterOtherUserIds(userIds: Array[String]): Array[String] =
    userIds.filter(_ != ojisanId)

  def addReactionToMessage(channel: SlackChannel, ts: String, emoji: String): IO[Unit] = IO {
    session.addReactionToMessage(channel, ts, emoji)
    ()
  }

  def onMessage(cb: (MessageEntity, SlackSession) => IO[Unit]): IO[Unit] = IO {
    session.addMessagePostedListener { (event, s) =>
      cb(MessageEntity(event), s) unsafeRunSync
    }
  }

  def onMessage(cb: MessageEntity => IO[Unit]): IO[Unit] =
    onMessage { (event, _) =>
      cb(event)
    }

  def onMessageAsync(): IO[MessageEntity] = Async[IO].async { cb =>
    session.addMessagePostedListener { (event, _) =>
      cb(Right(MessageEntity(event)))
    }
  }

  def onMessageFuture(): IO[Future[MessageEntity]] = IO {
    Promise[MessageEntity] match {
      case p =>
        session.addMessagePostedListener { (event, _) =>
          p.success(MessageEntity(event))
        }
        p.future
    }
  }

  def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageReply] =
    IO {
      session.sendMessage(channel, m).getReply
    }

  def helloOjisan(): Unit =
    IO {
      sendMessage(
        session.findChannelByName("random"),
        "よ〜〜〜し、オジサンがんばっちゃうゾ"
      )
    } unsafeRunAsyncAndForget
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
