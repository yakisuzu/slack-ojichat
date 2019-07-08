package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

trait OjisanMentionMessageService extends LazyLogging {
  implicit val repository: OjisanRepository
  implicit val ojichat: OjichatService

  def mentionMessage(): IO[Unit] =
    repository.onMessage {
      case message if !(message hasMention repository.ojisan) => IO.unit // オジサンあてじゃない
      case message =>
        ojichat
          .getTalk(Some(message.sender.realName))
          .flatMap { ojiTalk =>
            // FIXME メッセージ送信時刻の保持
            repository.sendMessage(message.channel, ojiTalk).map(_ => ())
          }
    }
}

object OjisanMentionMessageService {
  def apply()(
      implicit
      _repository: OjisanRepository,
      _ojichat: OjichatService
  ): OjisanMentionMessageService = new OjisanMentionMessageService() {
    override implicit val repository: OjisanRepository = _repository
    override implicit val ojichat: OjichatService      = _ojichat
  }
}
