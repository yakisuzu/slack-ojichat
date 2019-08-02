package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanKimagureMessageServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}

  override def afterEach() {}

  describe("kimagureMessage") {
    // 他所（OjisanMentionMessageService）で反応するので、ここでは沈黙が正解。
    it("オジさん宛てのメッセージ") {
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock("channelId"),
                timestamp = "",
                sender = UserValue("senderId", "senderName"),
                content = "<@ojisanId> ojisan?"
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        )
        override implicit val ojichat: OjichatService = new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = throw new AssertionError("こないで〜〜")
        }
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }
      }
      s.kimagureMessage() unsafeRunSync
    }
    it("自分の発言には反応しない") {
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock("channelId"),
                timestamp = "",
                sender = UserValue("ojisanId", ""),
                content = "<@chigauOjisan> ojisan?"
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        )
        override implicit val ojichat: OjichatService = new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = throw new AssertionError("こないで〜〜")
        }
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }
      }
      s.kimagureMessage() unsafeRunSync
    }
    it("急に湧いてくる") { //確率判定成功版
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock("channelId"),
                timestamp = "",
                sender = UserValue("senderId", "senderName"),
                content = "<@chigauOjisan> ojisan?"
              )
            ),
          sendMessageMock = (c: SlackChannel, m: String) =>
            IO {
              c.getId should be("channelId")
              m should be ("楽しそうだから来ちゃったヨ〜！　オジさんも混ぜてほしいナ！")

              SlackMessageReplyMock(ok = true)
            }
        )
        override implicit val ojichat: OjichatService = new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = IO {
            name.isDefined should be(true)
            name.foreach(_ should be("senderName"))
            "楽しそうだから来ちゃったヨ〜！　オジさんも混ぜてほしいナ！"
          }
        }
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }

      }
      s.kimagureMessage() unsafeRunSync
    }
    it("出しゃばりはモテない") { //確率判定失敗
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                channel = SlackChannelMock("channelId"),
                timestamp = "",
                sender = UserValue("senderId", "senderName"),
                content = "<@chigauOjisan> ojisan?"
              )
            ),
          sendMessageMock = (_, _) => throw new AssertionError("こないで〜〜")
        )
        override implicit val ojichat: OjichatService = new OjichatService {
          override def getTalk(name: Option[String]): IO[String] = throw new AssertionError("こないで〜〜")
        }
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(10) // ng
        }
      }
      s.kimagureMessage() unsafeRunSync
    }
  }
}
