package jp.ojisan

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackUser

object Main extends App with LazyLogging {
  lazy val conf        = ConfigFactory.load()
  lazy val ojisanName  = conf.getString("app.name")
  lazy val ojisanToken = conf.getString("app.slackToken")

  val ojisanService  = OjisanService(ojisanToken)
  val ojichatService = OjichatService()

  {
    ojisanService.debugMessage()

    // オジサンはかまってくれると嬉しい
    ojisanService.mentionedMessage { user: SlackUser =>
      ojichatService.getTalk(Some(user.getRealName)).unsafeRunSync()
    }

    // オジサンはかまいたい
    ojisanService.kimagureReaction()

    // オジサンはやさしい
    ojisanService.mentionRequest { at: String =>
      ojichatService.getTalk(Some(at)).unsafeRunSync()
    }

    logger.info("オジサン準備終わったヨ")
  }
}
