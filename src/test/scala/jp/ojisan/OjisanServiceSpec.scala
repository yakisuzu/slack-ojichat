package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
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
            _onMessage = (cb: SlackMessagePosted => IO[Unit]) =>
              cb(
                createSlackMessagePosted(
                  "mentionedOjisan",
                  null,
                  createSlackChannel("id", null),
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
        }
        s.mentionedMessage { (_, _) =>
          "sendContext"
        }
      }

      it("ojisan宛じゃないヨ") {
        val s = new OjisanService {
          override val repo: OjisanRepository = createOjisanRepository(
            _onMessage = (cb: SlackMessagePosted => IO[Unit]) =>
              cb(
                createSlackMessagePosted(
                  "ojisan...",
                  null,
                  createSlackChannel(null, null),
                  null
                )
              ),
            _hasOjisanMention = (_: SlackMessagePosted) => false,
            _sendMessage = (_: SlackChannel, _: String) => throw new AssertionError("こないで〜〜")
          )
        }
        s.mentionedMessage { (_, _) =>
          "sendContext"
        }
      }
    }

    describe("should kimagureReaction") { it("TODO") {} }

    describe("should debugMessage") { it("TODO") {} }
  }
}
