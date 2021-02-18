package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

trait OjisanMentionMessageService extends LazyLogging {
  val repository: OjisanRepository
  val ojichat: OjichatService

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
  def apply(
      _repository: OjisanRepository,
      _ojichat: OjichatService
  ): OjisanMentionMessageService = new OjisanMentionMessageService() {
    override val repository: OjisanRepository = _repository
    override val ojichat: OjichatService      = _ojichat
  }
}
