import akka.actor.ActorSystem
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("slack-ojichat")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val slackToken = sys.env.getOrElse("SLACK_TOKEN", "")
  val rtmService = new RtmService(SlackRtmClient(slackToken))
  val ojichatService = new OjichatService()

  println("よ〜〜〜し、オジサンがんばっちゃうゾ")

  // かまってくれた時
  rtmService.mentionedMessage { (user, _) =>
    ojichatService.getTalk(user.map(_.name)).unsafeRunSync()
  }
}
