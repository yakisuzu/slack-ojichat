import akka.actor.ActorSystem
import slack.rtm.SlackRtmClient

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("slack-ojichat")
  //  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val slackToken = sys.env.getOrElse("SLACK_TOKEN", "")
  val rtmService = new RtmService(SlackRtmClient(slackToken)(system))

  val ojichatService = new OjichatService()

  // FIXME websocketのconnectionつながったらね
  // 起きた時
  // val channel_random = "C40DE1SRW"
  // rtmService.sendMessage(channel_random, "よ〜〜〜し、オジサンがんばっちゃうゾ").unsafeRunSync()

  // かまってくれた時
  rtmService.mentionedMessage { (user, _) =>
    ojichatService.getTalk(user.flatMap(_.real_name)).unsafeRunSync()
  }

  println("ojisan end")
}
