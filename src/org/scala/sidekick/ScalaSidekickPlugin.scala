package org.scala.sidekick

import org.gjt.sp.jedit. {View, EBMessage, EBPlugin}
import scalariform.formatter.preferences.FormattingPreferences
import scalariform.formatter.ScalaFormatter
import scalariform.parser.ScalaParserException
import org.gjt.sp.jedit.textarea.JEditTextArea
import tools.refactoring.analysis.GlobalIndexes
import tools.refactoring.util.CompilerProvider
import tools.refactoring.implementations. {OrganizeImports, Rename}
import tools.refactoring.common.Change
import projectviewer. {ProjectViewer, ProjectPlugin}
import scala.collection.JavaConversions

object ScalaSidekickPlugin {
  val NAME = "ScalaSidekickPlugin"

  //Main only used for testing purpose

  def main(arg: Array[String]) {
    System.setProperty("scala.home", "C:/scala")
    val code = """package org.ensime.config
    import java.io.File
    import org.ensime.util._
    import org.ensime.util.FileUtils._
    import org.ensime.util.RichFile._
    import org.ensime.util.SExp._
    import scala.actors._
    import scala.actors.Actor._
    import scala.collection.mutable
    import scalariform.formatter.preferences._

    object ProjectConfig {}"""

    Refactoring.organizeImports(code)
  }

  def format(view: View) {
    Reformat.format(view)
  }

  def rename(textArea: JEditTextArea, view: View) {
    Refactoring.rename(textArea, view)
  }

  def organizeImports(textArea: JEditTextArea) {
    Refactoring.organizeImports(textArea)
  }

  def navigate(view: View) = {
    Navigation.createIndex(view)
  }


}

class ScalaSidekickPlugin extends EBPlugin {
  override def handleMessage(message: EBMessage) {}

  override def start {
    // Scala_Home should be set somewhere if I integrate SIndex it should be solved
    System.setProperty("scala.home", "C:/scala")
  }
}