package sqlpt.thriftplugin

import scala.util.{Success, Try}

object Utils {
  implicit class SequenceForTries[A](tries: Seq[Try[A]]) {
    def sequence: Try[List[A]] =
      tries.foldRight[Try[List[A]]](Success(Nil)) {(t, seq) =>
        for {
          t   <- t
          seq <- seq
        } yield t :: seq
      }
  }
}
