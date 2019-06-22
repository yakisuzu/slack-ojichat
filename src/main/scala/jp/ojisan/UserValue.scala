package jp.ojisan

import com.ullink.slack.simpleslackapi.SlackUser

case class UserValue(id: String, realName: String = "") {
  val contentUserId: String = s"<@$id>"
}

object UserValue {
  def apply(user: SlackUser): UserValue = new UserValue(
    id = user.getId,
    realName = user.getRealName
  )
}
