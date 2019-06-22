package jp.ojisan

import scala.util.matching.Regex

trait MessageContentUserService {
  val userIdRegex: Regex = """<@(\w+)>""".r

  def contentToUsers(message: MessageValue): Seq[UserValue] =
    userIdRegex
      .findAllMatchIn(message.content)
      .toSeq
      .map(_.subgroups.head)
      .map(UserValue(_))
}

object MessageContentUserService {
  def apply(): MessageContentUserService = new MessageContentUserService() {}
}
