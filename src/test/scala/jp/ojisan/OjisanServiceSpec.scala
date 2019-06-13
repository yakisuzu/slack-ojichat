package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  // http://www.scalatest.org/user_guide/using_matchers
  describe("mentionedMessage") {
    it("ojisan宛ダヨ") {
      val s = new OjisanService {
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanIdMock = "ojisanId",
          onMessageMock = (cb: MessageEntity => IO[Unit]) =>
            cb(
              new MessageEntity(
                SlackMessagePostedMock(
                  messageContent = "<@ojisanId> mentionedOjisan",
                  channel = SlackChannelMock("id")
                )
              )
            ),
          sendMessageMock = (c: SlackChannel, m: String) =>
            IO {
              c.getId should be("id")
              m should be("sendContext")
              SlackMessageReplyMock(ok = true)
            }
        )
      }
      s.mentionedMessage { _ =>
        "sendContext"
      }
    }

    it("ojisan宛じゃないヨ") {
      val s = new OjisanService {
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanIdMock = "ojisanId",
          onMessageMock = (cb: MessageEntity => IO[Unit]) =>
            cb(
              new MessageEntity(
                SlackMessagePostedMock(
                  messageContent = "<@chigauOjisan> ojisan?",
                  channel = SlackChannelMock()
                )
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        )
      }
      s.mentionedMessage { _ =>
        "sendContext"
      }
    }
  }

  describe("kimagureReaction") {
    it("反応できた") {
      val s = new OjisanService {
        override def rand100: Int          = 10 // ok
        override def choiceEmoji(): String = "emoji"
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanIdMock = "ojisanId",
          onMessageMock = (cb: MessageEntity => IO[Unit]) =>
            cb(
              new MessageEntity(
                SlackMessagePostedMock(
                  channel = SlackChannelMock(id = "ch"),
                  ts = "1234567"
                )
              )
            ),
          addReactionToMessageMock = (channel: SlackChannel, ts: String, emoji: String) =>
            IO {
              channel.getId should be("ch")
              ts should be("1234567")
              emoji should be("emoji")
              ()
            }
        )
      }
      s.kimagureReaction()
    }

    it("反応しない") {
      val sSelf = new OjisanService {
        override def rand100: Int = 10 // ok
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanIdMock = "ojisanId",
          onMessageMock = (cb: MessageEntity => IO[Unit]) =>
            cb(
              new MessageEntity(
                SlackMessagePostedMock(
                  user = SlackUserMock(id = "ojisanId") // 自分の発言
                )
              )
            ),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
      }
      sSelf.kimagureReaction()

      val sNg = new OjisanService {
        override def rand100: Int = 60 // ng
        override val repo: OjisanRepository = OjisanRepositoryMock(
          onMessageMock = (cb: MessageEntity => IO[Unit]) => cb(new MessageEntity(SlackMessagePostedMock())),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
      }
      sNg.kimagureReaction()
    }
  }

  describe("debugMessage") { it("TODO") {} }
}
