package jp.ojisan

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageValueSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("hasMention") {
    it("メンションあった") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = null,
        content = "<@mentionId>　oh yeah",
        threadTs = None
      )
      val actual = m.hasMention(new UserValue("mentionId", ""))
      actual should be(true)
    }

    it("メンションない") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = null,
        content = "<@mentionId>　oh yeah",
        threadTs = None
      )
      val actual = m.hasMention(new UserValue("chigauId", ""))
      actual should be(false)
    }
  }

  describe("talkedBy") {
    it("あってた") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = UserValue("ojisanId", "ojisan"),
        content = "",
        threadTs = None
      )
      val actual = m.talkedBy(new UserValue("ojisanId", ""))
      actual should be(true)
    }

    it("違うひとだった") {
      val m = new MessageValue(
        ts = "1",
        channel = null,
        timestamp = "",
        sender = UserValue("ojisanId", "ojisan"),
        content = "",
        threadTs = None
      )
      val actual = m.talkedBy(new UserValue("chigauId", ""))
      actual should be(false)
    }
  }
}
