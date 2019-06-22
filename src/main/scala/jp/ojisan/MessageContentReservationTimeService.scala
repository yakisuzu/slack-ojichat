package jp.ojisan

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import scala.util.matching.Regex
import scala.util.{Success, Try}

trait MessageContentReservationTimeService {
  val timeRegex: Regex                 = "[0-2][0-9]:[0-5][0-9]".r
  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  def contentToReservationTime(message: MessageValue): Option[ReservationTimeValue] =
    timeRegex
      .findAllIn(message.content)
      .map(c => Try(LocalTime.parse(c, timeFormatter)))
      .collectFirst { case Success(time) => ReservationTimeValue(time) }
}

object MessageContentReservationTimeService {
  def apply(): MessageContentReservationTimeService = new MessageContentReservationTimeService() {}
}
