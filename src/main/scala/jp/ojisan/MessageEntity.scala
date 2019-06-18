package jp.ojisan

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackUser}

import scala.util.matching.Regex
import scala.util.{Success, Try}

case class MessageEntity(message: SlackMessagePosted) {
  val channel: SlackChannel = message.getChannel
  val sender: SlackUser     = message.getSender
  val ts: String            = message.getTimestamp
  val context: String       = message.getMessageContent

  def hasMention(userId: String): Boolean =
    context contains userId

  def isTalk(userId: String): Boolean =
    sender.getId == userId

  def contextToUserIds: Seq[String] =
    MessageEntity.userIdRegex
      .findAllMatchIn(context)
      .toSeq
      .map { _.subgroups.head }

  def contextToTime: Option[String] =
    contextToLocalTime.map(_.format(MessageEntity.timeFormatter))

  def contextToLocalTime: Option[LocalTime] =
    MessageEntity.timeRegex
      .findAllIn(context)
      .map(c => Try(LocalTime.parse(c, MessageEntity.timeFormatter)))
      .collectFirst { case Success(time) => time }
}

object MessageEntity {
  val userIdRegex: Regex                       = """<@(\w+)>""".r
  val timeRegex: Regex                         = "[0-2][0-9]:[0-5][0-9]".r
  val timeFormatter: DateTimeFormatter         = DateTimeFormatter.ofPattern("HH:mm")
  def isContextUserId(userId: String): Boolean = userId.matches(userIdRegex.regex)
  def toContextUserId(userId: String): String  = if (isContextUserId(userId)) userId else s"<@$userId>"
}
