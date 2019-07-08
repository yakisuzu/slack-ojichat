package jp.ojisan

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging

import scala.util.Random

trait KimagureService extends LazyLogging {
  private val rand: Random   = new Random()
  def randN(n: Int): IO[Int] = IO(rand nextInt n)
}

object KimagureService {
  def apply(): KimagureService = new KimagureService() {}
}
