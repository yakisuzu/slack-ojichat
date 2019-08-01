package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

trait OjisanKimagureMessageService extends LazyLogging {
  implicit val repository: OjisanRepository
  implicit val ojichat: OjichatService
  implicit val kimagure: KimagureService

  def kimagureMessage(): IO[Unit] = {
    repository.onMessage {
      case message if message hasMention repository.ojisan => IO.unit // オジサンあてだった
      case message if message talkedBy repository.ojisan => IO.unit // オジサンの発言だった
      case message => {
        kimagure.randN(100).flatMap {
          case n if n < 95 => IO.unit // お呼びでない？
          case _ => ojichat.getTalk(Some(message.sender.realName)) // よ〜し、オジサン勇気出しちゃうゾ〜！
            .flatMap { ojiTalk =>
              repository.sendMessage(message.channel, ojiTalk).map(_ => ())
            }
        }
      }
    }
  }
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

