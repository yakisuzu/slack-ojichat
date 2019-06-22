package jp.ojisan

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted

case class MessageValue(channel: SlackChannel, timestamp: String, sender: UserValue, content: String) {
  def hasMention(user: UserValue): Boolean =
    content contains user.id

  def talkedBy(user: UserValue): Boolean =
    sender == user
}

object MessageValue {
  def apply(message: SlackMessagePosted): MessageValue =
    MessageValue(
      channel = message.getChannel,
      timestamp = message.getTimestamp,
      sender = UserValue(message.getSender.getId),
      content = message.getMessageContent
    )
}
