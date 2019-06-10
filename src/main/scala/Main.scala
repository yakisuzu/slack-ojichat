import com.typesafe.config.ConfigFactory

object Main extends App {
  lazy val conf        = ConfigFactory.load()
  lazy val ojisanName  = conf.getString("app.name")
  lazy val ojisanToken = conf.getString("app.slackToken")

  val ojisanService  = OjisanService(ojisanToken)
  val ojichatService = new OjichatService()

  {
    // ojisanService.helloOjisan()

    ojisanService.debugMessage()

    // オジサンはかまってくれると嬉しい
    ojisanService.mentionedMessage { (user, _) =>
      ojichatService.getTalk(Option(user.getRealName)).unsafeRunSync()
    }

    // オジサンはかまいたい
    ojisanService.kimagureReaction()

    println("ojisan end")
  }
}
