import akka.actor.{ActorRef, ActorSystem}
import cats.effect.IO
import slack.SlackUtil
import slack.models.{Message, UserProfile}
import slack.rtm.SlackRtmClient

import scala.concurrent.Future
import scala.util.Random

class RtmService(val client: SlackRtmClient, val system: ActorSystem) {
  lazy val ojisanId: String = client.state.self.id
  lazy val rand: Random = new Random()
  lazy val emojis: Map[String, String] = client.apiClient.listEmojis()(system)

  def debug(up: Option[UserProfile]): IO[Unit] = IO {
    val s = up.map(u => s"{ first_name: ${u.first_name}, last_name: ${u.last_name}, real_name: ${u.real_name} }")
    println(s)
  }

  def debug(m: Message): IO[Unit] = IO {
    println(s"{ ts: ${m.ts}, channel: ${m.channel}, user: ${m.user}, text: ${m.text} }")
  }

  def getUser(userId: String): Option[UserProfile] =
    client.state.getUserById(userId).flatMap(_.profile)

  def addReactionToMessage(emoji: String, m: Message): IO[Unit] = IO {
    client.apiClient.addReactionToMessage(emoji, m.channel, m.ts)(system) match {
      case _ => () // 結果はどうでもいい
    }
  }

  def onMessage(cb: Message => IO[Unit]): IO[ActorRef] = IO {
    client.onMessage(m => cb(m).unsafeRunSync())
  }

  //  def onMessage(): IO[Message] = Async[IO].async { cb =>
  //    client.onMessage(m => cb(Right(m)))
  //  }

  def sendMessage(channel: String, m: String): IO[Future[Long]] = IO {
    client.sendMessage(channel, m)
  }

  def mentionedMessage(makeMessage: (Option[UserProfile], Message) => String): ActorRef =
    onMessage { message =>
      for {
        _ <- debug(getUser(message.user))
        _ <- debug(message)
        _ <- SlackUtil.extractMentionedIds(message.text) match {
          case ids if ids.contains(ojisanId) => sendMessage(
            message.channel,
            makeMessage(getUser(message.user), message)
          )
          case _ => IO((): Unit)
        }
      } yield ()
    }.unsafeRunSync()

  def kimagureReaction(): ActorRef =
    onMessage { message =>
      (message.user, rand.nextInt(100)) match {
        case (`ojisanId`, _) => IO((): Unit) // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => addReactionToMessage(choiceEmoji(), message)
        case _ => IO((): Unit)
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
