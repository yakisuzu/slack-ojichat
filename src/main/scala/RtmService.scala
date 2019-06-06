import cats.effect.IO
import slack.SlackUtil
import slack.models.{Message, UserProfile}
import slack.rtm.SlackRtmClient

class RtmService(val client: SlackRtmClient) {
  val ojisanId: String = client.state.self.id

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
}
