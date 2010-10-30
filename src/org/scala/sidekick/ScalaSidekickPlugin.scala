package org.scala.sidekick

import org.gjt.sp.jedit. {View, EBMessage, EBPlugin}
import scalariform.formatter.preferences.FormattingPreferences
import scalariform.formatter.ScalaFormatter
import scalariform.parser.ScalaParserException
import org.gjt.sp.jedit.textarea.JEditTextArea
import tools.refactoring.analysis.GlobalIndexes
import tools.refactoring.implementations.Rename
import tools.refactoring.common.Change
import projectviewer. {ProjectViewer, ProjectPlugin}
import scala.collection.JavaConversions
import tools.refactoring.util. {CompilerInstance, TreeCreationMethods, CompilerProvider}
import projectviewer.vpt.VPTNode
import org.ensime.protocol.message. {GetConnectionInfo, InitProject}
import org.ensime.client. {ClientReceiver, ClientSender}
import org.ensime.server.Server

object ScalaSidekickPlugin {
  val NAME = "ScalaSidekickPlugin"

  private var PROCID = 0
  private var MSGID = 0

  def procCounter() =  {PROCID += 1;PROCID}
  def msgCounter() =  {MSGID += 1;MSGID}

  //Main only used for testing purpose
  //builder-update-files
  def main(arg: Array[String]) {
    System.setProperty("scala.home", "C:/scala")
    Navigation.loadIndex()


    ClientReceiver.start
    ClientSender.start

    Server

    ClientSender ! GetConnectionInfo(0)
    ClientSender ! InitProject("c:/Users/Stefan/Desktop/emacs-23.2/dist", "", "sbt", "d:/ensime/", 0)
  }

  def initProject(view: View) = {
    ClientReceiver.start
    ClientSender.start
    Server


    val projectPath = ProjectViewer.getActiveProject(view).getRootPath
    ClientSender ! GetConnectionInfo(0)
    ClientSender ! InitProject("c:/Users/Stefan/Desktop/emacs-23.2/dist", "", "sbt", projectPath, msgCounter())
  }


  def organizeImports(src: String) {
  }

  def format(view: View) {
    Reformat.format(view)
  }

  def rename(textArea: JEditTextArea, view: View) {
    Refactoring.rename(textArea, view)
  }

  def organizeImports(textArea: JEditTextArea, view: View) {
    Refactoring.organizeImports(textArea, view)
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
    Navigation.loadIndex()
  }
}