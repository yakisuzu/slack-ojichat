object Main extends App {
  lazy val ojisanName = getEnvRequire("HEROKU_APP_NAME")
  lazy val ojisanToken = getEnvRequire("SLACK_TOKEN")

  val rtmService = RtmService.init(ojisanToken, ojisanName)
  val ojichatService = new OjichatService()

  // FIXME websocketのconnectionつながったらね
  // 起きた時
  // val channel_random = "C40DE1SRW"
  // rtmService.sendMessage(channel_random, "よ〜〜〜し、オジサンがんばっちゃうゾ").unsafeRunSync()

  // オジサンはかまってくれると嬉しい
  rtmService.mentionedMessage { (user, _) =>
    ojichatService.getTalk(user.flatMap(_.real_name)).unsafeRunSync()
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
