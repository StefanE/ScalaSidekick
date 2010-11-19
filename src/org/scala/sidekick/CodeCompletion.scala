package org.scala.sidekick

import java.awt.Component
import java.awt.Font
import java.awt.Point
import java.awt.event.KeyEvent
import java.util.HashSet
import java.util.Set
import java.util.TreeSet
import java.util.Arrays
import java.util.Collection
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.SwingUtilities
import org.gjt.sp.jedit.gui.CompletionPopup
import org.gjt.sp.jedit.visitors.JEditVisitorAdapter
import org.gjt.sp.jedit.syntax.KeywordMap
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.gjt.sp.util.StandardUtilities
import org.gjt.sp.jedit.gui.CompletionPopup.Candidates
import org.gjt.sp.jedit._

class CodeCompletion(view: View, location: Point, val firstList: List[String], var word: String)
  extends CompletionPopup(view, location) {
  private var buffer: Buffer = null
  private val textArea = view.getTextArea

  private def resetWords(newWord: String) {

    //var caret = textArea.getCaretPosition
    //var completions = getCompletions(buffer, newWord, caret)
    val newList = firstList.filter(_.startsWith(newWord))
    val options = new Options(textArea, newList)

    if (newList.length > 0) {
      word = newWord
      reset(options, true)
    }
    else {
      println("DISPOSE1" + newWord)
      //dispose
    }
  }

  protected override def keyPressed(e: KeyEvent): Unit = {
    if (e.getKeyCode == KeyEvent.VK_BACK_SPACE) {
      textArea.backspace
      e.consume
      if (word.length == 0) {
        println("DISPOSE2")
        //dispose
      }
      else {

        word = word.substring(0, word.length - 1)
        resetWords(word)
      }
    }
  }

  protected override def keyTyped(e: KeyEvent): Unit = {
    var ch: Char = e.getKeyChar
    /*if (Character.isDigit(ch)) {
      var index: Int = ch - '0'
      if (index == 0) index = 9
      else ({
        index -= 1;
        index
      })
      if (index < getCandidates.getSize) {
        setSelectedIndex(index)
        if (doSelectedCompletion) {
          e.consume
          dispose
        }
        return
      }
    }*/
    if (ch != '\b' && ch != '\t') {
      if (!Character.isLetterOrDigit(ch) /*&& noWordSep.indexOf(ch) == -1*/ ) {
        doSelectedCompletion
        textArea.userInput(ch)
        e.consume
        println("DISPOSE3")
        dispose
        return
      }
      textArea.userInput(ch)
      e.consume
      word += ch
      resetWords(word)
    }
  }
}

private case class Completion(val text: String, val keyword: Boolean)

private class Options(val textArea: JEditTextArea, val options: List[String]) extends Candidates {

  private val renderer = new DefaultListCellRenderer()

  def complete(index: Int) {
    val completion = options(index)
    val fillIn = completion.split(' ')(0)
    textArea.selectWord
    textArea.setSelectedText(fillIn);
  }

  def getCellRenderer(list: JList, index: Int, isSelected: Boolean, cellHasFocus: Boolean) = {
    renderer.getListCellRendererComponent(
      list, null, index, isSelected, cellHasFocus);
    val font = list.getFont
    val text = options(index)

    renderer.setText(text);
    renderer.setFont(font);

    renderer
  }

  def getDescription(index: Int) = ""

  def getSize = options.size

  def isValid = true
}

/*
private case class Words(var completion: Array[Completion]) extends Candidates {
  val renderer = new DefaultListCellRenderer

  def getSize = completions.length

  def isValid = true

  def complete(index: Int): Unit = {
    var insertion: String = completions(index).toString.substring(word.length)
    textArea.replaceSelection(insertion)
  }

  def getCellRenderer(list: JList, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
    renderer.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus)
    var comp: Completion = completions(index)
    var text: String = comp.text
    var font: Font = list.getFont
    if (index < 9) text = (index + 1) + ": " + text
    else if (index == 9) text = "0: " + text
    if (comp.keyword) font = font.deriveFont(Font.BOLD)
    renderer.setText(text)
    renderer.setFont(font)
    renderer
  }

  def getDescription(index: Int) = ""
}
*/
