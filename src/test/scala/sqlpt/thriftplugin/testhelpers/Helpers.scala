package sqlpt.thriftplugin.testhelpers

import java.util.StringTokenizer
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._
import sqlpt.thriftplugin.Utils.BasicThriftParser
import treehugger.forest._

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

trait Helpers extends BasicThriftParser with CodeTreeMatchers
