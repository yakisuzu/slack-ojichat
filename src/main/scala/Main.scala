import com.typesafe.config.ConfigFactory

object Main extends App {
  lazy val conf = ConfigFactory.load()
  lazy val ojisanName = conf.getString("app.name")
  lazy val ojisanToken = conf.getString("app.slackToken")

  val rtmService = RtmService(ojisanToken)
  val ojichatService = new OjichatService()

  {
    // rtmService.helloOjisan()

    rtmService.debugMessage()

    // オジサンはかまってくれると嬉しい
    rtmService.mentionedMessage { (user, _) =>
      ojichatService.getTalk(Option(user.getRealName)).unsafeRunSync()
    }

    // オジサンはかまいたい
    rtmService.kimagureReaction()

    println("ojisan end")
  }
}
