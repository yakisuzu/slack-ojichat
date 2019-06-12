package jp.ojisan

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackUser}

import scala.util.{Success, Try}

case class MessageEntity(message: SlackMessagePosted) {
  val channel: SlackChannel = message.getChannel
  val sender: SlackUser     = message.getSender
  val ts: String            = message.getTimestamp
  val context: String       = message.getMessageContent

  private val splitedContext: Array[String] = message.getMessageContent split "\\s"

  def hasMention(id: String): Boolean =
    context contains id

  def isTalk(id: String): Boolean =
    sender.getId == id

  def contextToIds: Array[String] =
    splitedContext.filter(_.matches("<@[a-z,A-Z,0-9]+>"))

  def contextToTime: Option[LocalTime] =
    splitedContext
      .map { c =>
        Try(LocalTime.parse(c, DateTimeFormatter.ofPattern("HH:mm")))
      }
      .collectFirst {
        case Success(t) => t
      }
}
