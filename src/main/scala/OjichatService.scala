import scala.sys.process.Process

class OjichatService() {
  def getTalk(name: Option[String]): String =
    Process(s"ojichat ${name.getOrElse("")}").!!
}
