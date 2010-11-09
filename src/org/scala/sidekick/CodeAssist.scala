package org.scala.sidekick

import java.awt.Point
import javax.swing.{JList, DefaultListCellRenderer}
import org.gjt.sp.jedit.gui.CompletionPopup.Candidates
import org.gjt.sp.jedit.{GUIUtilities, View}
import org.ensime.protocol.message.{TypeAtPoint, ScopeCompletion, TypeCompletion}
import org.ensime.client.{Global, ClientSender}
import org.gjt.sp.jedit.gui.CompletionPopup
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.scala.sidekick.ScalaSidekickPlugin._

object CodeAssist {
  def complete(textArea:JEditTextArea, view:View) {
    view.getBuffer.save(view,null)
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
        //relPos += 1
      } else {relPos += 1}

      ClientSender ! TypeCompletion(file, caret-relPos, word, msgID)      
    }
    else {ClientSender ! ScopeCompletion(file, currentCarPos, word, false, msgID)}
  }
   
  def getType(textArea:JEditTextArea, view:View) {
    view.getBuffer.autosave
    val currentCarPos = textArea.getCaretPosition()
    val file = view.getBuffer.getPath
    val id = msgCounter()
    
    Global.actions += id -> {(list:List[String]) => {
      val Type = Array[AnyRef](list(0))
      //TODO: Should show in a nicer way
      GUIUtilities.message(null,"info.typeInfo",Type)
    } }
    
    ClientSender ! TypeAtPoint(file,currentCarPos,id)
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
