package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanKimagureMessageServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}

  override def afterEach() {}

  describe("onMessageAction") {
    it("オジさん宛てのメッセージ") {
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", "")
        )
        override implicit val ojichat: OjichatService = new OjichatService {}
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }
        // 他所（OjisanMentionMessageService）で反応するので、ここでは沈黙が正解。
        override def sendMessage(c: SlackChannel, name: String): IO[Unit] = throw new AssertionError("こないで〜〜")
      }
      val m = new MessageValue(
        channel = SlackChannelMock(),
        timestamp = "",
        sender = UserValue("senderId", "senderName"),
        content = "<@ojisanId> ojisan?"
      )
      s.onMessageAction(m) unsafeRunSync
    }
    it("自分の発言には反応しない") {
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", "")
        )
        override implicit val ojichat: OjichatService = new OjichatService {}
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }
        override def sendMessage(c: SlackChannel, name: String): IO[Unit] = throw new AssertionError("こないで〜〜")
      }
      val m = new MessageValue(
        channel = SlackChannelMock(),
        timestamp = "",
        sender = UserValue("ojisanId", ""),
        content = "<@chigauOjisan> ojisan?"
      )
      s.onMessageAction(m) unsafeRunSync
    }
    it("出しゃばりはモテない") { // 確率判定失敗
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", "")
        )
        override implicit val ojichat: OjichatService = new OjichatService {}
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(10) // ng
        }
        override def sendMessage(c: SlackChannel, name: String): IO[Unit] = throw new AssertionError("こないで〜〜")
      }
      val m = new MessageValue(
        channel = SlackChannelMock(),
        timestamp = "",
        sender = UserValue("senderId", "senderName"),
        content = "<@chigauOjisan> ojisan?"
      )
      s.onMessageAction(m) unsafeRunSync
    }
    it("急に湧いてくる") { // 確率判定成功版
      val s = new OjisanKimagureMessageService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", "")
        )
        override implicit val ojichat: OjichatService = new OjichatService {}
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(99) // ok
        }
        override def sendMessage(c: SlackChannel, name: String): IO[Unit] = {
          c.getId should be("channelId")
          name should be("senderName")
          IO.unit
        }
      }
      val m = new MessageValue(
        channel = SlackChannelMock("channelId"),
        timestamp = "",
        sender = UserValue("senderId", "senderName"),
        content = "<@chigauOjisan> ojisan?"
      )
      s.onMessageAction(m) unsafeRunSync
    }
  }
}
