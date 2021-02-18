package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanMentionMessageServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  // http://www.scalatest.org/user_guide/using_matchers
  describe("mentionMessage") {
    it("ojisan宛ダヨ") {
      val s = OjisanMentionMessageService(
        OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                ts = "1",
                channel = SlackChannelMock("channelId"),
                timestamp = "",
                sender = UserValue("senderId", "senderName"),
                content = "<@ojisanId> mentionedOjisan",
                threadTs = None
              )
            ),
          sendMessageMock = (c: SlackChannel, m: String) =>
            IO {
              c.getId should be("channelId")
              m should be("呼んだ？呼んだよね？")

              SlackMessageReplyMock(ok = true)
            }
        ),
        new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = IO {
            name.isDefined should be(true)
            name.foreach(_ should be("senderName"))

            "呼んだ？呼んだよね？"
          }
        }
      )

      s.mentionMessage() unsafeRunSync
    }

    it("ojisan宛じゃないヨ") {
      val s = OjisanMentionMessageService(
        OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                ts = "1",
                channel = SlackChannelMock(),
                timestamp = "",
                sender = UserValue("", ""),
                content = "<@chigauOjisan> ojisan?",
                threadTs = None
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        ),
        new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = throw new AssertionError("こないで〜〜")
        }
      )

      s.mentionMessage() unsafeRunSync
    }
  }
}
