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
  def complete(textArea: JEditTextArea, view: View) {
    //TODO: Make some kind og save, else user should do it manually
    val msgID = msgCounter()

    val caret = textArea.getCaretPosition()
    val line = textArea.getCaretLine
    val lineTxt = textArea.getLineText(line)
    var word = ""
    val file = view.getBuffer.getPath
    var msgToSend: AnyRef = null
    //val caret = textArea.getCaretPosition()
    //var word = textArea.getText(caret, (currentCarPos - caret))

    if (lineTxt.contains(".")) {
      val text = textArea.getText(caret - 1, 1)
      if (text == ".") {
        println("###TEST1")
        textArea.setCaretPosition(caret,false)
        msgToSend = TypeCompletion(file, caret - 1, "", msgID)
      }
      else {
        println("###TEST2")
        textArea.goToPrevWord(true)
        val curCaret = textArea.getCaretPosition
        word = textArea.getText(curCaret, caret - curCaret)
        msgToSend = TypeCompletion(file, curCaret - 1, word, msgID)
      }
    }
    else {
      println("###TEST3")
      textArea.goToPrevWord(true)
      val curCaret = textArea.getCaretPosition
      word = textArea.getText(curCaret, caret - curCaret)
      msgToSend = ScopeCompletion(file, curCaret, word, false, msgID)
    }

    //executes when answer returns
    Global.actions += msgID -> {
      (list: List[String]) => {
        val pos = textArea.getLocationOnScreen
        val relpos = textArea.offsetToXY(caret)
        val position = new Point((pos.getX + relpos.getX + 40).toInt, (pos.getY + relpos.getY).toInt)
        val options = new Options(view.getTextArea, list)
        val completion = new CodeCompletion(view, position, list, word)
        completion.reset(options, true)
      }
    }
    ClientSender ! msgToSend
  }

  def getType(textArea: JEditTextArea, view: View) {
    //view.getBuffer.autosave
    //Thread.sleep(300)
    if (view.getBuffer.isDirty)
      GUIUtilities.message(null, "info.save", null)
    else {
      val currentCarPos = textArea.getCaretPosition()
      val file = view.getBuffer.getPath
      val id = msgCounter()

      Global.actions += id -> {
        (list: List[String]) => {
          val Type = Array[AnyRef](list(0))
          //TODO: Should show in a nicer way
          GUIUtilities.message(null, "info.typeInfo", Type)
        }
      }

      ClientSender ! TypeAtPoint(file, currentCarPos, id)
    }
  }
}