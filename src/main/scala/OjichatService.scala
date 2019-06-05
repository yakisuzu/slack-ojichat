import cats.effect.IO

import scala.sys.process.Process

class OjichatService() {
  def getTalk(name: Option[String]): IO[String] = IO {
    Process(s"ojichat ${name.getOrElse("")}").!!
  }
}
