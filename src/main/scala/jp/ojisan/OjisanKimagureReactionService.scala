package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

trait OjisanKimagureReactionService extends LazyLogging {
  val repository: OjisanRepository
  val kimagure: KimagureService

  def kimagureReaction(): IO[Unit] =
    repository.onMessage {
      case message if message talkedBy repository.ojisan => IO.unit // 自分の発言にはリアクションしない
      case message =>
        kimagure.randN(100).flatMap {
          case n if n > 50 => IO.unit // 気まぐれで反応しない
          case _ =>
            choiceEmoji().flatMap { emoji =>
              repository.addReactionToMessage(message.channel, message.timestamp, emoji)
            }
        }
    }

  def choiceEmoji(): IO[String] =
    kimagure.randN(repository.emojis.size).map { i =>
      repository.emojis.zipWithIndex.find(_._2 == i).map(_._1).get
    }
}

object OjisanKimagureReactionService {
  def apply(
      _repository: OjisanRepository
  ): OjisanKimagureReactionService = new OjisanKimagureReactionService() {
    override val repository: OjisanRepository = _repository
    override val kimagure: KimagureService    = KimagureService()
  }
}
