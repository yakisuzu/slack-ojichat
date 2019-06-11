package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

import scala.sys.process.Process

class OjichatService() extends LazyLogging {
  def makeCommand(args: String*): Seq[String] =
    Seq("ojichat") ++ args

  def getTalk(name: Option[String]): IO[String] = IO {
    name match {
      case Some(n) => Process(makeCommand(n)) !!
      case _       => Process(makeCommand()) !!
    }
  }
}
