package jp.ojisan

import cats.effect.{Async, IO}
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackMessageHandle, SlackSession, SlackUser}

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}

class OjisanRepository(val session: SlackSession) extends LazyLogging {
  private lazy val ojisanId: String = session.sessionPersona().getId
  lazy val emojis: Set[String]      = session.listEmoji().getReply.getEmojis.keySet().asScala.toSet

  def hasOjisanMention(m: SlackMessagePosted): Boolean =
    m.getMessageContent.contains(ojisanId)

  def isOjiTalk(m: SlackMessagePosted): Boolean =
    m.getSender.getId == ojisanId

  def getUser(userId: String): Option[SlackUser] =
    Option(session.findUserById(userId))

  def addReactionToMessage(emoji: String, m: SlackMessagePosted): IO[Unit] = IO {
    session.addReactionToMessage(m.getChannel, m.getTimestamp, emoji)
    ()
  }

  def onMessage(cb: (SlackMessagePosted, SlackSession) => IO[Unit]): IO[Unit] = IO {
    session.addMessagePostedListener { (event, s) =>
      cb(event, s) unsafeRunSync
    }
  }

  def onMessage(cb: SlackMessagePosted => IO[Unit]): IO[Unit] =
    onMessage { (event, _) =>
      cb(event)
    }

  def onMessageAsync(): IO[SlackMessagePosted] = Async[IO].async { cb =>
    session.addMessagePostedListener { (event, _) =>
      cb(Right(event))
    }
  }

  def onMessageFuture(): IO[Future[SlackMessagePosted]] = IO {
    Promise[SlackMessagePosted] match {
      case p =>
        session.addMessagePostedListener { (event, _) =>
          p.success(event)
        }
        p.future
    }
  }

  def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageHandle[SlackMessageReply]] =
    IO {
      session.sendMessage(channel, m)
    }

  def helloOjisan(): Unit =
    IO {
      if (session.isConnected) {
        sendMessage(
          session.findChannelByName("random"),
          "よ〜〜〜し、オジサンがんばっちゃうゾ"
        )
        ()
      } else {
        Thread.sleep(1000)
        helloOjisan()
      }
    } unsafeRunSync
}

object OjisanRepository {
  def apply(ojisanToken: String): OjisanRepository = {
    val s = SlackSessionFactory.createWebSocketSlackSession(ojisanToken)
    s.connect()
    new OjisanRepository(s)
  }
}
