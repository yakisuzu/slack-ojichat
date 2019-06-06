import cats.effect.IO

import scala.sys.process.Process

class OjichatService() {
  def getTalk(name: Option[String]): IO[String] = IO {
    name match {
      case Some(n) => Process(s"ojichat '$n'").!!
      case _ => Process(s"ojichat").!!
    }
  }
}
