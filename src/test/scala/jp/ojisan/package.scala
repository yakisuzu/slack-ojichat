package jp

import cats.effect.IO
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession, SlackUser}

package object ojisan {
  private[ojisan] def createSlackChannel(id: String, name: String): SlackChannel =
    new SlackChannel(id, name, null, null, false, false, false)

  private[ojisan] def createSlackMessagePosted(
      messageContent: String,
      user: SlackUser,
      channel: SlackChannel,
      timestamp: String
  ): SlackMessagePosted =
    new SlackMessagePosted(messageContent, null, user, channel, timestamp, null)

  private[ojisan] def createSlackMessageReply(ok: Boolean, timestamp: String): SlackMessageReply =
    new SlackMessageReply(ok, null, 0, timestamp)

  private[ojisan] def createOjisanRepository(
      _onMessage: (SlackMessagePosted => IO[Unit]) => IO[Unit],
      _hasOjisanMention: SlackMessagePosted => Boolean,
      _sendMessage: (SlackChannel, String) => IO[SlackMessageReply]
  ): OjisanRepository = new OjisanRepository {
    override val session: SlackSession =
      null

    override def onMessage(cb: SlackMessagePosted => IO[Unit]): IO[Unit] =
      _onMessage(cb)

    override def hasOjisanMention(m: SlackMessagePosted): Boolean =
      _hasOjisanMention(m)

    override def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageReply] =
      _sendMessage(channel, m)
  }
}
