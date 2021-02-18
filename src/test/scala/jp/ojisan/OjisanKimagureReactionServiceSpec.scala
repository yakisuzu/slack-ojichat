package jp.ojisan

import cats.effect.IO
import com.ullink.slack.simpleslackapi.SlackChannel
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class OjisanKimagureReactionServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("kimagureReaction") {
    it("反応できた") {
      val s = new OjisanKimagureReactionService() {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                ts = "1",
                channel = SlackChannelMock(id = "ch"),
                timestamp = "1234567",
                sender = UserValue("", ""),
                content = "",
                threadTs = None
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
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(10) // ok
        }
        override def choiceEmoji(): IO[String] = IO("emoji")
      }

      s.kimagureReaction() unsafeRunSync
    }

    it("自分の発言には反応しない") {
      val sSelf = new OjisanKimagureReactionService {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          ojisanMock = UserValue("ojisanId", ""),
          onMessageMock = (cb: MessageValue => IO[Unit]) =>
            cb(
              new MessageValue(
                ts = "1",
                channel = null,
                timestamp = "",
                sender = UserValue("ojisanId", ""), // 自分の発言
                content = "",
                threadTs = None
              )
            ),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(10) // ok
        }
      }

      sSelf.kimagureReaction() unsafeRunSync
    }

    it("ランダムで反応しない") {
      val sNg = new OjisanKimagureReactionService {
        override implicit val repository: OjisanRepository = OjisanRepositoryMock(
          onMessageMock = (cb: MessageValue => IO[Unit]) => cb(MessageValue(SlackMessagePostedMock())),
          addReactionToMessageMock = (_, _, _) => throw new AssertionError("こないで〜〜〜")
        )
        override implicit val kimagure: KimagureService = new KimagureService {
          override def randN(n: Int): IO[Int] = IO(60) // ng
        }
      }

      sNg.kimagureReaction() unsafeRunSync
    }
  }
}
