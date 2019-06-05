import akka.actor.ActorRef
import slack.SlackUtil
import slack.models.{Message, User}
import slack.rtm.SlackRtmClient

class RtmService(val client: SlackRtmClient) {
  val ojisanId: String = client.state.self.id

  def getUser(userId: String): Option[User] =
    client.state.getUserById(userId)

  def mentionedMessage(makeMessage: (Option[User], Message) => String): ActorRef =
    client.onMessage { message =>
      val mentioned = SlackUtil.extractMentionedIds(message.text).contains(ojisanId)
      if (mentioned) {
        client.sendMessage(message.channel, makeMessage(getUser(message.user), message))
      }
    }
}
