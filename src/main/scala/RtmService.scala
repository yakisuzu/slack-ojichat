import akka.actor.ActorRef
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

class RtmService(val client: SlackRtmClient) {
  val ojisanId: String = client.state.self.id

  def getUserName(userId: String): Option[String] =
    client.state.getUserById(userId).map(_.name)

  def mentionedMessage(makeMessage: Message => String): ActorRef =
    client.onMessage { message =>
      val mentioned = SlackUtil.extractMentionedIds(message.text).contains(ojisanId)
      if (mentioned) {
        client.sendMessage(message.channel, makeMessage(message))
      }
    }
}
