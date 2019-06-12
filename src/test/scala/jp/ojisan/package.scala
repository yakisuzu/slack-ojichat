package jp

import cats.effect.IO
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession, SlackUser}

package object ojisan {
  private[ojisan] def createSlackChannel(id: String, name: String): SlackChannel =
    new SlackChannel(id, name, null, null, false, false, false)

  private[ojisan] def createMessageEntity(
      messageContent: String,
      user: SlackUser,
      channel: SlackChannel,
      ts: String
  ): MessageEntity =
    MessageEntity(new SlackMessagePosted(messageContent, null, user, channel, ts, null))

  private[ojisan] def createSlackMessageReply(ok: Boolean, timestamp: String): SlackMessageReply =
    new SlackMessageReply(ok, null, 0, timestamp)

  private[ojisan] def createOjisanRepository(
      _ojisanId: String,
      _onMessage: (MessageEntity => IO[Unit]) => IO[Unit],
      _sendMessage: (SlackChannel, String) => IO[SlackMessageReply]
  ): OjisanRepository = new OjisanRepository {
    override val session: SlackSession                              = null
    override lazy val ojisanId: String                              = _ojisanId
    override def onMessage(cb: MessageEntity => IO[Unit]): IO[Unit] = _onMessage(cb)
    override def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageReply] =
      _sendMessage(channel, m)
  }
}
