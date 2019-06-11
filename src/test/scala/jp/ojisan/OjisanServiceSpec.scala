package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply
import com.ullink.slack.simpleslackapi.{SlackChannel, SlackSession, SlackUser}
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  // http://www.scalatest.org/user_guide/using_matchers
  describe("OjisanServiceSpec") {
    describe("should mentionedMessage") {
      it("ojisan宛ダヨ") {
        val s = createService(
          _onMessage = (cb: SlackMessagePosted => IO[Unit]) =>
            cb(
              createSlackMessagePosted(
                "mentionedOjisan",
                null,
                createSackChannel("id", null),
                null
              )
            ),
          _hasOjisanMention = (_: SlackMessagePosted) => true,
          _sendMessage = (c: SlackChannel, m: String) =>
            IO {
              c.getId should be("id")
              m should be("sendContext")
              createSlackMessageReply(ok = true, null)
            }
        )
        s.mentionedMessage { (_, _) =>
          "sendContext"
        }
      }

      it("ojisan宛じゃないヨ") {
        val s = createService(
          _onMessage = (cb: SlackMessagePosted => IO[Unit]) =>
            cb(
              createSlackMessagePosted(
                "ojisan...",
                null,
                createSackChannel(null, null),
                null
              )
            ),
          _hasOjisanMention = (_: SlackMessagePosted) => false,
          _sendMessage = (_: SlackChannel, _: String) => throw new AssertionError("こないで〜〜")
        )
        s.mentionedMessage { (_, _) =>
          "sendContext"
        }
      }
    }

    describe("should kimagureReaction") { it("TODO") {} }

    describe("should debugMessage") { it("TODO") {} }
  }

  private[this] def createSackChannel(id: String, name: String): SlackChannel =
    new SlackChannel(id, name, null, null, false, false, false)

  private[this] def createSlackMessagePosted(
      messageContent: String,
      user: SlackUser,
      channel: SlackChannel,
      timestamp: String
  ): SlackMessagePosted =
    new SlackMessagePosted(messageContent, null, user, channel, timestamp, null)

  private[this] def createSlackMessageReply(ok: Boolean, timestamp: String): SlackMessageReply =
    new SlackMessageReply(ok, null, 0, timestamp)

  private[this] def createService(
      _onMessage: (SlackMessagePosted => IO[Unit]) => IO[Unit],
      _hasOjisanMention: SlackMessagePosted => Boolean,
      _sendMessage: (SlackChannel, String) => IO[SlackMessageReply]
  ): OjisanService =
    new OjisanService {
      override val repo: OjisanRepository = new OjisanRepository {
        override val session: SlackSession =
          null

        override def onMessage(cb: SlackMessagePosted => IO[Unit]): IO[Unit] =
          _onMessage(cb)

        override def hasOjisanMention(m: SlackMessagePosted): Boolean =
          _hasOjisanMention(m)

        override def sendMessage(
            channel: SlackChannel,
            m: String
        ): IO[SlackMessageReply] =
          _sendMessage(channel, m)
      }
    }
}
