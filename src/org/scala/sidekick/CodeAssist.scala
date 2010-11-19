package org.scala.sidekick

import java.awt.Point
import org.gjt.sp.jedit.gui.CompletionPopup.Candidates
import org.gjt.sp.jedit.{GUIUtilities, View}
import org.ensime.client.{Global, ClientSender}
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.scala.sidekick.ScalaSidekickPlugin._
import java.awt.event.{KeyEvent, KeyAdapter}
import org.gjt.sp.jedit.gui.{CompleteWord, CompletionPopup}
import javax.swing.{SwingUtilities, JList, DefaultListCellRenderer}
import org.ensime.protocol.message.{TypecheckFile, TypeAtPoint, ScopeCompletion, TypeCompletion}

object CodeAssist {
  def complete(textArea:JEditTextArea, view:View) {
    //Make some kind og save, else user should do it manually
    //view.getBuffer.save(view,null)
    //Thread.sleep(200)
    /*view.getBuffer.save(view,null)*/
    //view.getBuffer.autosave()
    /*Thread.sleep(200)   */
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
      val position = new Point((pos.getX+relpos.getX+40).toInt, (pos.getY+relpos.getY).toInt)
      
      //var position: Point = textArea.offsetToXY(caret - word.length)
      //position.y += textArea.getPainter.getFontMetrics.getHeight
      //SwingUtilities.convertPointToScreen(position, textArea.getPainter)
      
      val options = new Options(view.getTextArea,list)
      /*
      val completion = new CompletionPopup(view, position)

      completion.reset(options,true)
      
       */
      val completion = new CodeCompletion(view,position,list,word)
      completion.reset(options,true)

    } }
    textArea.setCaretPosition(currentCarPos)
    if(lineTxt.contains('.')) {
      var relPos = 0
      if(word == ".") {
        word = ""
        //textArea.setCaretPosition(caret+1)
      } else {relPos += 1}

      ClientSender ! TypeCompletion(file, caret-relPos, word, msgID)      
    }
    else {ClientSender ! ScopeCompletion(file, caret, word, false, msgID)}
  }
   
  def getType(textArea:JEditTextArea, view:View) {
    //view.getBuffer.autosave

    //view.getBuffer.autosave()
    //Thread.sleep(300)
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

