package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

sealed abstract class DebugMessageMode[F[_]](val printMessage: Boolean) extends LazyLogging {
  type F[_] = IO[F[_]]

  def debugMessage(ojisanRepository: OjisanRepository)(implicit io: IO[F]): F[Unit] =
    if (printMessage) ojisanRepository.onMessage(debug)
    else IO.unit


//  def fmap[F[_], A, B](fa: F[A])(f: A => B)(implicit ft: Functor[F]) =

  def debug(m: MessageValue): F[Unit] = IO {
    logger.debug(
      Map(
        "ts"                 -> m.timestamp,
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
  def apply[F <: IO[_]](mode: String): DebugMessageMode[F] = mode match {
    case m: String if m == "ON" => new DebugMessageModeOn
    case _                      => new DebugMessageModeOff
  }

}

case class DebugMessageModeOn[F]()  extends DebugMessageMode[F](true)
case class DebugMessageModeOff[F]() extends DebugMessageMode[F](false)
