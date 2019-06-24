package jp.ojisan

import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterEach, FunSpec}

class MessageContentUserServiceSpec extends FunSpec with BeforeAndAfterEach {
  override def beforeEach() {}
  override def afterEach() {}

  describe("contentToUsers") {
    it("userIdが取れる") {
      val c      = new MessageValue(channel = null, timestamp = "", sender = null, content = "<@dare> <@dore>　oh yeah")
      val actual = MessageContentUserService().contentToUsers(c)
      actual should be(Seq(UserValue("dare", ""), UserValue("dore", "")))
    }
  }
}
