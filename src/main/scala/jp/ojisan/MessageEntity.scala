package jp.ojisan

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackUser}

case class MessageEntity(message: SlackMessagePosted) {
  val channel: SlackChannel = message.getChannel
  val sender: SlackUser     = message.getSender
  val ts: String            = message.getTimestamp
  val context: String       = message.getMessageContent

  private val splitedContext: Array[String] = message.getMessageContent split "\\s"

  def hasMention(id: String): Boolean =
    context contains id

  def getIds: Array[String] =
    splitedContext.filter(_.matches("<@[a-z,A-Z,0-9]+>"))

  def isTalk(id: String): Boolean =
    sender.getId == id
}
