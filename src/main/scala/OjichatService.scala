import cats.effect.IO

import scala.sys.process.Process

class OjichatService() {
  def makeCommand(args: String*): Seq[String] =
    Seq("ojichat") ++ args

  def getTalk(name: Option[String]): IO[String] = IO {
    name match {
      case Some(n) => Process(makeCommand(n)) !!
      case _ => Process(makeCommand()) !!
    }
  }
}
