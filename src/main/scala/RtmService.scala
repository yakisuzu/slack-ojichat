import cats.effect.{Async, IO}
import slack.SlackUtil
import slack.models.{Message, UserProfile}
import slack.rtm.SlackRtmClient

class RtmService(val client: SlackRtmClient) {
  val ojisanId: String = client.state.self.id

  def debug(m: Message): IO[Unit] = IO {
    println(m)
  }

  def getUser(userId: String): Option[UserProfile] =
    client.state.getUserById(userId).flatMap(_.profile)

  def onMessage(): IO[Message] = Async[IO].async { cb =>
    client.onMessage(m => cb(Right(m)))
  }

  def sendMessage(channel: String, m: String): IO[Unit] = IO {
    client.sendMessage(channel, m)
  }

  def mentionedMessage(makeMessage: (Option[UserProfile], Message) => String): Unit =
  // FIXME onMessage().runAsync { case Right(message) => IO( ... ); case Left(_) => IO() }.unsafeRunSync()
    client.onMessage(message =>
      (for {
        _ <- debug(message)
        _ <- SlackUtil.extractMentionedIds(message.text) match {
          case ids if ids.contains(ojisanId) => sendMessage(
            message.channel,
            makeMessage(getUser(message.user), message)
          )
        }
      } yield ()).unsafeRunSync()
    )
}
