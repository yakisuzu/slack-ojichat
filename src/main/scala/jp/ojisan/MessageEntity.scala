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

  private val splitContext: Array[String] = message.getMessageContent split "\\s"

  def hasMention(userId: String): Boolean =
    context contains userId

  def isTalk(userId: String): Boolean =
    sender.getId == userId

  def contextToUserIds: Array[String] =
    splitContext
      .map { c =>
        MessageEntity.userIdFormatter.findFirstIn(c)
      }
      .collect {
        case Some(userId) => userId
      }

  def contextToTime: Option[String] =
    splitContext
      .map { c =>
        Try(LocalTime.parse(c, MessageEntity.timeFormatter))
      }
      .collectFirst {
        case Success(time) => time.format(MessageEntity.timeFormatter)
      }
}

object MessageEntity {
  val timeFormatter: DateTimeFormatter         = DateTimeFormatter.ofPattern("HH:mm")
  val userIdFormatter: Regex                   = "<@[a-z,A-Z,0-9]+>".r
  def isContextUserId(userId: String): Boolean = userId.matches(userIdFormatter.regex)
  def toContextUserId(userId: String): String  = if (isContextUserId(userId)) userId else s"<@$userId>"
}
