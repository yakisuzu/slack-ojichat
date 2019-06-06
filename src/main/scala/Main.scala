import akka.actor.ActorSystem
import slack.rtm.SlackRtmClient

object Main extends App {
  lazy val ojisanName = getEnvRequire("HEROKU_APP_NAME")
  lazy val ojisanToken = getEnvRequire("SLACK_TOKEN")

  implicit val ojisanSystem: ActorSystem = ActorSystem(ojisanName)
  //  implicit val ec: ExecutionContextExecutor = ojisanSystem.dispatcher

  val rtmService = new RtmService(SlackRtmClient(ojisanToken)(ojisanSystem))
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

  def getEnvRequire(key: String): String =
    sys.env.get(key) match {
      case Some(token) => token
      case _ => throw new IllegalArgumentException(s"${key}がなくてオジサンさびしいヨ")
    }
}
