package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel

trait OjisanThreadMessageService {
  val repository: OjisanRepository
  val ojichat: OjichatService

  def alwaysMessage(): IO[Unit] =
    repository.onMessage {
      case message if message talkedBy repository.ojisan => IO.unit // 自分の発言にはリアクションしない
      case message if message.threadTs.isDefined => // threadの発言だった！！
        sendThreadMessage(message.channel, message.sender.realName, message.threadTs.get)
      case _ => IO.unit
    }

  def sendThreadMessage(c: SlackChannel, name: String, parentTs: String): IO[Unit] =
    for {
      ojiTalk <- ojichat.getTalk(Some(name))
      _       <- repository.sendThreadMessage(c, parentTs, ojiTalk)
    } yield ()
}

object OjisanThreadMessageService {
  def apply(
      _repository: OjisanRepository,
      _ojichat: OjichatService
  ): OjisanThreadMessageService = new OjisanThreadMessageService() {
    override val repository: OjisanRepository = _repository
    override val ojichat: OjichatService      = _ojichat
  }
}
