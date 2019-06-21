package jp.ojisan

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageValueSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("contextToUserIds") {
    it("userIdが取れる") {
      val s      = new MessageValue(SlackMessagePostedMock(messageContent = "<@dare> <@dore>　oh yeah"))
      val actual = s.contextToUserIds
      actual should be(Seq("dare", "dore"))
    }
  }

  describe("contextToTime") {
    it("時間が取れる") {
      val s      = new MessageValue(SlackMessagePostedMock(messageContent = "<@dare> <@dore>　oh yeah 01:30"))
      val actual = s.contextToTime
      actual should be(Some("01:30"))
    }
  }
}
