package jp.ojisan

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object Main extends App with LazyLogging {
  lazy val conf        = ConfigFactory.load()
  lazy val ojisanName  = conf.getString("app.name")
  lazy val ojisanToken = conf.getString("app.slackToken")

  val ojisanService  = OjisanService(ojisanToken)
  val ojichatService = OjichatService()

  {
    ojisanService.debugMessage()

    // オジサンはかまってくれると嬉しい
    ojisanService.mentionedMessage { (user, _) =>
      ojichatService.getTalk(Option(user.getRealName)).unsafeRunSync()
    }

    // オジサンはかまいたい
    ojisanService.kimagureReaction()

    logger.info("オジサン準備終わったヨ")
  }
}
