package sqlpt.thriftplugin

import com.twitter.scrooge.frontend.{NullImporter, ThriftParser}

import scala.util.parsing.input.{CharArrayReader, Reader}
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

  trait BasicThriftParser {
    def toReader(str: String): Reader[Char] =
      new CharArrayReader(str.toCharArray)

    val thriftParser = new ThriftParser(NullImporter, true)
  }
}
