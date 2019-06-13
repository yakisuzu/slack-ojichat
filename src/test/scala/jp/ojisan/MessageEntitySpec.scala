package jp.ojisan

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageEntitySpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("contextToUserIds") {
    it("userIdが取れる") {
      val s      = new MessageEntity(SlackMessagePostedMock(messageContent = "<@dare> <@dore>　oh yeah"))
      val actual = s.contextToUserIds
      actual should be(Array("dare", "dore"))
    }
  }

  describe("contextToTime") {}
}
