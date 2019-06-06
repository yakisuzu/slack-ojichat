import akka.actor.ActorSystem
import cats.effect.IO
import slack.SlackUtil
import slack.models.{Message, UserProfile}
import slack.rtm.SlackRtmClient

import scala.util.Random

class RtmService(val client: SlackRtmClient, val system: ActorSystem) {
  lazy val ojisanId: String = client.state.self.id
  lazy val rand: Random = new Random()
  lazy val emojis = client.apiClient.listEmojis()(system)

  def debug(up: Option[UserProfile]): IO[Unit] = IO {
    val s = up.map(u => s"{ first_name: ${u.first_name}, last_name: ${u.last_name}, real_name: ${u.real_name} }")
    println(s)
  }

  def debug(m: Message): IO[Unit] = IO {
    println(s"{ ts: ${m.ts}, channel: ${m.channel}, user: ${m.user}, text: ${m.text} }")
  }

  def getUser(userId: String): Option[UserProfile] =
    client.state.getUserById(userId).flatMap(_.profile)

  def onMessage(cb: Message => IO[Unit]): IO[Unit] = IO {
    client.onMessage(m => cb(m).unsafeRunSync())
  }

  //  def onMessage(): IO[Message] = Async[IO].async { cb =>
  //    client.onMessage(m => cb(Right(m)))
  //  }

  def sendMessage(channel: String, m: String): IO[Unit] = IO {
    client.sendMessage(channel, m)
  }

  def mentionedMessage(makeMessage: (Option[UserProfile], Message) => String): Unit =
    onMessage { message =>
      for {
        _ <- debug(getUser(message.user))
        _ <- debug(message)
        _ <- SlackUtil.extractMentionedIds(message.text) match {
          case ids if ids.contains(ojisanId) => sendMessage(
            message.channel,
            makeMessage(getUser(message.user), message)
          )
          case _ => IO()
        }
      } yield ()
    }.unsafeRunSync()

  def kimagureReaction(): Unit =
    onMessage { message =>
      (message.user, rand.nextInt(100)) match {
        case (`ojisanId`, _) => IO() // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => IO {
          client.apiClient.addReactionToMessage(choiceEmoji(), message.channel, message.ts)(system)
        }
        case _ => IO()
      }
    }.unsafeRunSync()

  def choiceEmoji(): String = {
    val i = rand.nextInt(emojis.keys.size)
    emojis.keys.zipWithIndex.find(_._2 == i).map(_._1).get
  }

}

object RtmService {
  def init(ojisanToken: String, ojisanName: String): RtmService = {
    lazy implicit val ojisanSystem: ActorSystem = ActorSystem(ojisanName)
    // implicit val ec: ExecutionContextExecutor = ojisanSystem.dispatcher

    new RtmService(SlackRtmClient(ojisanToken)(ojisanSystem), ojisanSystem)
  }
}
