import akka.actor.{ActorRef, ActorSystem}
import cats.effect.IO
import slack.SlackUtil
import slack.models.{Message, UserProfile}
import slack.rtm.SlackRtmClient

import scala.concurrent.Future
import scala.util.Random

class RtmService(val client: SlackRtmClient)(implicit val system: ActorSystem) {
  private val rand: Random = new Random()
  private lazy val ojisanId: String = client.state.self.id
  private lazy val emojis: Iterable[String] = client.apiClient.listEmojis().keys

  def getUser(userId: String): Option[UserProfile] =
    client.state.getUserById(userId).flatMap(_.profile)

  def addReactionToMessage(emoji: String, m: Message): IO[Unit] = IO {
    client.apiClient.addReactionToMessage(emoji, m.channel, m.ts)
    ()
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
      SlackUtil.extractMentionedIds(message.text) match {
        case ids if ids.contains(ojisanId) =>
          sendMessage(
            message.channel,
            makeMessage(getUser(message.user), message)
          ).unsafeRunSync()
          // FIXME メッセージ送信時刻の保持
          IO(())
        case _ => IO(())
      }
    }.unsafeRunSync()

  def kimagureReaction(): ActorRef =
    onMessage { message =>
      (message.user, rand.nextInt(100)) match {
        case (`ojisanId`, _) => IO(()) // 自分の発言にはリアクションしない
        case (_, n) if n < 50 => addReactionToMessage(choiceEmoji(), message)
        case _ => IO(())
      }
    }.unsafeRunSync()

  def choiceEmoji(): String = {
    val i = rand.nextInt(emojis.size)
    emojis.zipWithIndex.find(_._2 == i).map(_._1).get
  }

  def debugMessage(): ActorRef =
    onMessage { message =>
      for {
        _ <- debug(getUser(message.user))
        _ <- debug(message)
      } yield ()
    }.unsafeRunSync()

  def debug(up: Option[UserProfile]): IO[Unit] = IO {
    up match {
      case Some(u) => println(s"{ first_name: ${u.first_name.getOrElse("")}, last_name: ${u.last_name.getOrElse("")}, real_name: ${u.real_name.getOrElse("")} }")
      case _ => ()
    }
  }

  def debug(m: Message): IO[Unit] = IO {
    println(s"{ ts: ${m.ts}, channel: ${m.channel}, user: ${m.user}, text: ${m.text} }")
  }
}

object RtmService {
  def init(ojisanToken: String, ojisanName: String): RtmService = {
    lazy implicit val ojisanSystem: ActorSystem = ActorSystem(ojisanName)
    // implicit val ec: ExecutionContextExecutor = ojisanSystem.dispatcher

    new RtmService(SlackRtmClient(ojisanToken)(ojisanSystem))(ojisanSystem)
  }
}
