package sqlpt.thriftplugin.testhelpers

import java.util.StringTokenizer
import com.twitter.scrooge.frontend.{NullImporter, ThriftParser}
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._
import treehugger.forest._
import scala.util.parsing.input.{CharArrayReader, Reader}

trait TestingThriftParser {
  def toReader(str: String): Reader[Char] =
    new CharArrayReader(str.toCharArray)

  val thriftParser = new ThriftParser(NullImporter, true)
}

trait CodeTreeMatchers {
  def beSameScalaCodeAs(codeStr: String): Matcher[Tree] = {
    def tokenized(sourceCode: String): List[String] = {
      val Delimiters = " \n()[]{},;:"

      def toList(t: StringTokenizer): List[String] =
        if(t.hasMoreTokens)
          t.nextToken :: toList(t)
        else
          Nil

      toList(new StringTokenizer(sourceCode, Delimiters, true)).filterNot(_.trim.isEmpty)
    }

    (
      (tree: Tree) =>
        tokenized(codeStr) == tokenized(treeToString(tree)),

      (tree: Tree) =>
        s"${treeToString(tree)}\n\nis not the same scala code as:\n$codeStr"
    )
  }
}

trait Helpers extends TestingThriftParser with CodeTreeMatchers
