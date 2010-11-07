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

  def format(view: View) {
    if(isInitialized(view))
      Reformat.format(view)
  }

  def getType(textArea:JEditTextArea, view:View) {
    view.getBuffer.autosave
    val currentCarPos = textArea.getCaretPosition()
    val file = view.getBuffer.getPath
    val id = msgCounter()
    
    Global.actions += id -> {(list:List[String]) => {
      val Type = Array[AnyRef](list(0))
      //Should show in a nicer way
      GUIUtilities.message(null,"info.typeInfo",Type)
    } }
    
    ClientSender ! TypeAtPoint(file,currentCarPos,id)
  }
  
  def complete(textArea:JEditTextArea, view:View) {
    view.getBuffer.autosave
    val currentCarPos = textArea.getCaretPosition()
    textArea.goToPrevWord(true)
    val file = view.getBuffer.getPath
    val caret = textArea.getCaretPosition()
    var word = textArea.getText(caret, (currentCarPos-caret))
    
    val line = textArea.getCaretLine
    val lineTxt = textArea.getLineText(line)
    val msgID = msgCounter()
    
    //Should be executed when answer returns
    Global.actions += msgID -> {(list:List[String]) => {
      val pos =  textArea.getLocationOnScreen
      val relpos = textArea.offsetToXY(caret)
      val position = new Point(
      (pos.getX+relpos.getX+40).toInt
      , (pos.getY+relpos.getY).toInt)
      val completion = new CompletionPopup(view, position)
      completion.reset(new Options(view.getTextArea,list),true)      
    } }
    
    if(lineTxt.contains('.')) {
      var relPos = 0
      if(word == ".") {
        word = ""
        textArea.setCaretPosition(caret+1)      
      } else {relPos += 1}

      ClientSender ! TypeCompletion(file, caret-relPos, word, msgID)      
    }
    else {ClientSender ! ScopeCompletion(file, currentCarPos, word, false, msgID)}
  }

  def rename(textArea: JEditTextArea, view: View) {
    if(isInitialized(view))
      Refactoring.rename(textArea, view)
  }

  def organizeImports(textArea: JEditTextArea, view: View) {
    if(isInitialized(view))
      Refactoring.organizeImports(textArea, view)
  }

  def navigate(view: View) = {
    Navigation.createIndex(view)
  }

  private def isInitialized(view: View) = {
    if(Global.initialized){
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
    // Scala_Home should be set somewhere if I integrate SIndex it should be solved
    System.setProperty("scala.home", "C:/scala")
    Navigation.loadIndex()
  }
}

class Options(val textArea:JEditTextArea, val options:List[String]) extends Candidates {

  private val renderer = new DefaultListCellRenderer()

  def complete(index : Int) {
    val completion = options(index)
    //Filter what is actually completed
    val fillIn = completion.split(' ')(0)
    textArea.setSelectedText(fillIn);
  }
  
  def getCellRenderer(list:JList, index: Int, isSelected:Boolean,cellHasFocus:Boolean ) = {
      renderer.getListCellRendererComponent(
        list, null, index,isSelected, cellHasFocus);
    val font = list.getFont
    val text = options(index)
        
    renderer.setText(text);
    renderer.setFont(font);
    
    renderer
  }
  
  def getDescription(index:Int) = ""  
  def getSize = options.size
  def isValid = true
}