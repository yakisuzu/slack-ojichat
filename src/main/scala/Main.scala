import akka.actor.ActorSystem
import slack.SlackUtil
import slack.rtm.SlackRtmClient

import scala.sys.process.Process

object Main extends App {
  implicit val system = ActorSystem("slack-ojichat")
  implicit val ec = system.dispatcher

  val slackToken = sys.env.getOrElse("SLACK_TOKEN", "")
  val client = SlackRtmClient(slackToken)
  val ojichanId = client.state.self.id

  println("よ〜〜〜し、おじさんがんばっちゃうゾ")
  client.onMessage { message =>
    val mentionedOjichan = SlackUtil.extractMentionedIds(message.text).contains(ojichanId)

    if (mentionedOjichan) {
      val userName = for {
        u <- client.state.getUserById(message.user)
      } yield u.name
      // val userName = s"<@${message.user}>"
      val ojiTalk = Process(s"ojichat ${userName.getOrElse("")}").!!
      println(ojiTalk)
      client.sendMessage(message.channel, ojiTalk)
    }
  }
}
