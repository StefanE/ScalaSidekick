package org.scala.sidekick

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
import org.ensime.server.Server
import org.gjt.sp.jedit.{GUIUtilities, View, EBMessage, EBPlugin}
import org.ensime.client.{Global, ClientReceiver, ClientSender}
import org.gjt.sp.jedit.gui.CompletionPopup
import org.gjt.sp.jedit.gui.CompletionPopup.Candidates
import javax.swing.{JList, DefaultListCellRenderer}
import java.awt.Point
import org.ensime.protocol.message._

object ScalaSidekickPlugin {
  val NAME = "ScalaSidekickPlugin"

  private var PROCID = 0
  private var MSGID = 0

  def procCounter() =  {PROCID += 1;PROCID}
  def msgCounter() =  {MSGID += 1;MSGID}

  def initProject(view: View) = {
    ClientReceiver.start
    ClientSender.start
    Server

    var projectPath = ProjectViewer.getActiveProject(view).getRootPath
    
    projectPath = GUIUtilities.input(null,"info.init.confirm",null,projectPath)
    
    ClientSender ! GetConnectionInfo(0)
    ClientSender ! InitProject("c:/Users/Stefan/Desktop/emacs-23.2/dist", "", "sbt", projectPath, msgCounter())
  }

  def format(view: View) {
    if(Initialized(view))
      Reformat.format(view)
  }

  def getType(textArea:JEditTextArea, view:View) {
    if(Initialized(view)) {
      CodeAssist.getType(textArea,view)
    }
  }
  
  def complete(textArea:JEditTextArea, view:View) {
    if(Initialized(view))
      CodeAssist.complete(textArea,view)
  }

  def rename(textArea: JEditTextArea, view: View) {
    if(Initialized(view))
      Refactoring.rename(textArea, view)
  }

  def organizeImports(textArea: JEditTextArea, view: View) {
    if(Initialized(view))
      Refactoring.organizeImports(textArea, view)
  }

  def navigate(view: View) = {
    if(Initialized(view))
      Navigation.createIndex(view)
  }

  private def Initialized(view: View) = {
    if(Global.initialized) {
        true
    }
    else {
      val projectRoot = ProjectViewer.getActiveProject(view).getRootPath
      val arr = Array[AnyRef](projectRoot)
      GUIUtilities.error(null, "error.noInit", arr)
      false
    }
  }
}

class ScalaSidekickPlugin extends EBPlugin {
  override def handleMessage(message: EBMessage) {}

  override def start {
    Navigation.loadIndex()
  }
}

