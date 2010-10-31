package org.scala.sidekick

import scalariform.parser.ScalaParserException
import scalariform.formatter.ScalaFormatter
import org.gjt.sp.jedit.View
import scalariform.formatter.preferences.FormattingPreferences
import org.ensime.protocol.message.ReformatFile
import org.ensime.client.{Global, ClientSender}

object Reformat  {

  def format(view:View) {
    setCurrent(view)
    val path = view.getBuffer.getPath
    ClientSender ! ReformatFile(path,ScalaSidekickPlugin.msgCounter)
  }

  private def setCurrent(view:View) {
    Global.currentView = view
    Global.currentBuffer = view.getBuffer
  }

 /*
  def format(view: View) {
      val buffer = view.getBuffer
      val length = buffer.getLength
      val unformattedText = buffer.getText(0, length)
      val preferences = FormattingPreferences()

      var isFormatted = false
      var formattedText = ""

      try {
        formattedText = ScalaFormatter.format(unformattedText, preferences)
        isFormatted = true
      } catch {
        case e: ScalaParserException => println("Syntax error in Scala source")
      }

      if (isFormatted) {
        buffer.writeLock
        buffer.remove(0, length)
        buffer.insert(0, formattedText)
        buffer.writeUnlock
      }
    }  */



}