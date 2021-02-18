package jp.ojisan

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

case class MessageValue(ts: String, channel: SlackChannel, timestamp: String, sender: UserValue, content: String, threadTs: Option[String]) {
  def hasMention(user: UserValue): Boolean =
    content contains user.id

  def talkedBy(user: UserValue): Boolean =
    sender.id == user.id
}

object MessageValue {
  def apply(message: SlackMessagePosted): MessageValue =
    MessageValue(
      ts = message.getTimeStamp,
      channel = message.getChannel,
      timestamp = message.getTimestamp,
      sender = UserValue(message.getSender),
      content = message.getMessageContent,
      threadTs = Option(message.getThreadTimestamp)
    )
}
