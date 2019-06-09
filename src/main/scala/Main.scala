object Main extends App {
  lazy val ojisanName = getEnvRequire("HEROKU_APP_NAME")
  lazy val ojisanToken = getEnvRequire("SLACK_TOKEN")

  val rtmService = RtmService(ojisanToken)
  val ojichatService = new OjichatService()

  // rtmService.helloOjisan()

  rtmService.debugMessage()

  // オジサンはかまってくれると嬉しい
  rtmService.mentionedMessage { (user, _) =>
    ojichatService.getTalk(Option(user.getRealName)).unsafeRunSync()
  }

  // オジサンはかまいたい
  rtmService.kimagureReaction()

  println("ojisan end")

  def getEnvRequire(key: String): String =
    sys.env.get(key) match {
      case Some(token) => token
      case _ => throw new IllegalArgumentException(s"${key}がなくてオジサンさびしいヨ")
    }
}
