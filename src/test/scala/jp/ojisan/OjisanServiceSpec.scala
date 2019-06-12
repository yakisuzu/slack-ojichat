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
          override val repo: OjisanRepository = createOjisanRepository(
            _ojisanId = "ojisanId",
            _onMessage = (cb: MessageEntity => IO[Unit]) =>
              cb(
                createMessageEntity(
                  "<@ojisanId> mentionedOjisan",
                  null,
                  createSlackChannel("id", null),
                  null
                )
              ),
            _sendMessage = (c: SlackChannel, m: String) =>
              IO {
                c.getId should be("id")
                m should be("sendContext")
                createSlackMessageReply(ok = true, null)
              }
          )
        }
        s.mentionedMessage { _ =>
          "sendContext"
        }
      }

      it("ojisan宛じゃないヨ") {
        val s = new OjisanService {
          override val repo: OjisanRepository = createOjisanRepository(
            _ojisanId = "ojisanId",
            _onMessage = (cb: MessageEntity => IO[Unit]) =>
              cb(
                createMessageEntity(
                  "<@chigauOjisan> ojisan?",
                  null,
                  createSlackChannel(null, null),
                  null
                )
              ),
            _sendMessage = (_: SlackChannel, _: String) => throw new AssertionError("こないで〜〜")
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
