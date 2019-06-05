import cats.effect.{Async, IO, SyncIO}
import slack.SlackUtil
import slack.models.{Message, User}
import slack.rtm.SlackRtmClient

class RtmService(val client: SlackRtmClient) {
  val ojisanId: String = client.state.self.id

  def debug(m: Message): IO[Unit] = IO {
    println(m)
  }

  def getUser(userId: String): Option[User] =
    client.state.getUserById(userId)

  def onMessage(): IO[Message] = Async[IO].async { cb =>
    client.onMessage(m => cb(Right(m)))
  }

  def sendMessage(channel: String, m: String): IO[Unit] = IO {
    client.sendMessage(channel, m)
  }

  def mentionedMessage(makeMessage: (Option[User], Message) => String): SyncIO[Unit] =
    onMessage().runAsync {
      case Right(message) => for {
        _ <- debug(message)
        _ <- SlackUtil.extractMentionedIds(message.text) match {
          case ids if ids.contains(ojisanId) => sendMessage(
            message.channel,
            makeMessage(getUser(message.user), message)
          )
          case _ => IO()
        }
      } yield ()
      case Left(_) => IO()
    }
}
