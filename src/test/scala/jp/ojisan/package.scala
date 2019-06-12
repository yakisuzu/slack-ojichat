package jp

import cats.effect.IO
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackPersona, SlackSession, SlackUser}

package object ojisan {
  private[ojisan] object SlackUserMock {
    def apply(
        id: String = "",
        realName: String = ""
    ): SlackUser =
      new SlackUser {
        override def getId: String                           = id
        override def getUserName: String                     = ""
        override def getRealName: String                     = realName
        override def getUserMail: String                     = ""
        override def getUserSkype: String                    = ""
        override def getUserPhone: String                    = ""
        override def getUserTitle: String                    = ""
        override def isDeleted: Boolean                      = false
        override def isAdmin: Boolean                        = false
        override def isOwner: Boolean                        = false
        override def isPrimaryOwner: Boolean                 = false
        override def isRestricted: Boolean                   = false
        override def isUltraRestricted: Boolean              = false
        override def isBot: Boolean                          = false
        override def getTimeZone: String                     = ""
        override def getTimeZoneLabel: String                = ""
        override def getTimeZoneOffset: Integer              = 0
        override def getPresence: SlackPersona.SlackPresence = null
      }
  }

  private[ojisan] object SlackChannelMock {
    def apply(
        id: String = "",
        name: String = ""
    ): SlackChannel =
      new SlackChannel(id, name, null, null, false, false, false)
  }

  private[ojisan] object MessageEntityMock {
    def apply(
        messageContent: String = "",
        user: SlackUser = SlackUserMock(),
        channel: SlackChannel = SlackChannelMock(),
        ts: String = ""
    ): MessageEntity =
      MessageEntity(new SlackMessagePosted(messageContent, null, user, channel, ts, null))
  }

  private[ojisan] object SlackMessageReplyMock {
    def apply(
        ok: Boolean = false,
        timestamp: String = ""
    ): SlackMessageReply =
      new SlackMessageReply(ok, null, 0, timestamp)
  }

  private[ojisan] object OjisanRepositoryMock {
    def apply(
        ojisanIdMock: String,
        onMessageMock: (MessageEntity => IO[Unit]) => IO[Unit],
        sendMessageMock: (SlackChannel, String) => IO[SlackMessageReply]
    ): OjisanRepository = new OjisanRepository {
      override val session: SlackSession                              = null
      override lazy val ojisanId: String                              = ojisanIdMock
      override def onMessage(cb: MessageEntity => IO[Unit]): IO[Unit] = onMessageMock(cb)
      override def sendMessage(channel: SlackChannel, m: String): IO[SlackMessageReply] =
        sendMessageMock(channel, m)
    }
  }
}
