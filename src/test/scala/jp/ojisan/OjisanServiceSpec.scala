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
          ojisanMock = UserValue("ojisanId"),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock("id"),
                timestamp = "",
                sender = null,
                content = "<@ojisanId> mentionedOjisan"
              )
            ),
          sendMessageMock = (c: SlackChannel, m: String) =>
            IO {
              c.getId should be("id")
              m should be("sendContent")
              SlackMessageReplyMock(ok = true)
            }
        )
      }
      s.mentionedMessage { _ =>
        "sendContent"
      } unsafeRunSync
    }

    it("ojisan宛じゃないヨ") {
      val s = new OjisanService {
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId"),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock(),
                timestamp = "",
                sender = null,
                content = "<@chigauOjisan> ojisan?"
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        )
      }
      s.mentionedMessage { _ =>
        "sendContent"
      } unsafeRunSync
    }
  }

  describe("kimagureReaction") {
    it("反応できた") {
      val s = new OjisanService {
        override def rand100: Int          = 10 // ok
        override def choiceEmoji(): String = "emoji"
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId"),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock(id = "ch"),
                timestamp = "1234567",
                sender = null,
                content = ""
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
      s kimagureReaction () unsafeRunSync
    }

    it("反応しない") {
      val sSelf = new OjisanService {
        override def rand100: Int = 60 // ok
        override val repo: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId"),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = null,
                timestamp = "",
                sender = UserValue("ojisanId"), // 自分の発言
                content = ""
              )
            ),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
      }
      sSelf.kimagureReaction() unsafeRunSync

      val sNg = new OjisanService {
        override def rand100: Int = 10 // ng
        override val repo: OjisanRepository = OjisanRepositoryMock(
          onMessageMock = (cb: MessageValue => IO[Unit]) => cb(MessageValue(SlackMessagePostedMock())),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
      }
      sNg.kimagureReaction() unsafeRunSync
    }
  }

  describe("mentionRequest") {}
}
