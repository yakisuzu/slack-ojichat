package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

sealed abstract class DebugMessageMode(val printMessage: Boolean) extends LazyLogging {
  def debugMessage(ojisanRepository: OjisanRepository): IO[Unit] =
    if (printMessage) ojisanRepository.onMessage(debug)
    else IO.unit

  def debug(m: MessageValue): IO[Unit] = IO {
    logger.debug(
      Map(
        "ts"                 -> m.timestamp,
        "threadTs"           -> m.threadTs,
        "channelId"          -> m.channel.getId,
        "channelName"        -> m.channel.getName,
        "senderUserId"       -> m.sender.id,
        "senderUserRealName" -> m.sender.realName,
        "content"            -> m.content
      ).toString()
    )
  }
}

object DebugMessageMode {
  def apply(mode: String): DebugMessageMode = mode match {
    case m: String if m == "ON" => new DebugMessageModeOn
    case _                      => new DebugMessageModeOff
  }
}

case class DebugMessageModeOn()  extends DebugMessageMode(true)
case class DebugMessageModeOff() extends DebugMessageMode(false)
