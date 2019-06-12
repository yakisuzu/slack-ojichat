package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  // http://www.scalatest.org/user_guide/using_matchers
  describe("OjisanServiceSpec") {
    describe("should mentionedMessage") {
      it("ojisan宛ダヨ") {
        val s = new OjisanService {
          override val repo: OjisanRepository = OjisanRepositoryMock(
            ojisanIdMock = "ojisanId",
            onMessageMock = (cb: MessageEntity => IO[Unit]) =>
              cb(
                MessageEntityMock(
                  messageContent = "<@ojisanId> mentionedOjisan",
                  channel = SlackChannelMock("id")
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
                MessageEntityMock(
                  messageContent = "<@chigauOjisan> ojisan?",
                  channel = SlackChannelMock()
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

    describe("should kimagureReaction") { it("TODO") {} }

    describe("should debugMessage") { it("TODO") {} }
  }
}
