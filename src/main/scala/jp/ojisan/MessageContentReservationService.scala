package jp.ojisan

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import scala.util.matching.Regex
import scala.util.{Success, Try}

trait MessageContentReservationService {
  implicit val messageContentUser: MessageContentUserService

  val timeRegex: Regex                 = "[0-2][0-9]:[0-5][0-9]".r
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  def contentToReservationTime(message: MessageValue): Option[ReservationTimeValue] =
    timeRegex
      .findAllIn(message.content)
      .map(c => Try(LocalTime.parse(c, timeFormatter)))
      .collectFirst { case Success(time) => ReservationTimeValue(time) }

  def contextToReservationUsers(m: MessageValue, ojisan: UserValue): Seq[UserValue] = {
    messageContentUser
      .contentToUsers(m)
      .filter(_.id != ojisan.id)
  }
}

object MessageContentReservationService {
  def apply(): MessageContentReservationService = new MessageContentReservationService() {
    override implicit val messageContentUser: MessageContentUserService = new MessageContentUserService {}
  }
}
