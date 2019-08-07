package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.ullink.slack.simpleslackapi.SlackChannel

trait OjisanKimagureMessageService extends LazyLogging {
  implicit val repository: OjisanRepository
  implicit val ojichat: OjichatService
  implicit val kimagure: KimagureService

  def kimagureMessage(): IO[Unit] =
    repository.onMessage(onMessageAction)

  def onMessageAction(message: MessageValue): IO[Unit] =
    message match {
      case message if message hasMention repository.ojisan => IO.unit // オジサンあてだった
      case message if message talkedBy repository.ojisan   => IO.unit // オジサンの発言だった
      case message =>
        kimagure.randN(100).flatMap {
          case n if n < 95 => IO.unit // お呼びでない？
          case _ => // よ〜し、オジサン勇気出しちゃうゾ〜！
            sendMessage(message.channel, message.sender.realName)
        }
    }

  def sendMessage(c: SlackChannel, name: String): IO[Unit] =
    for {
      ojiTalk <- ojichat.getTalk(Some(name))
      _       <- repository.sendMessage(c, ojiTalk)
    } yield ()
}

object OjisanKimagureMessageService {
  def apply()(
      implicit
      _repository: OjisanRepository,
      _ojichat: OjichatService
  ): OjisanKimagureMessageService = new OjisanKimagureMessageService() {
    override implicit val repository: OjisanRepository = _repository
    override implicit val ojichat: OjichatService      = _ojichat
    override implicit val kimagure: KimagureService    = KimagureService()
  }
}
