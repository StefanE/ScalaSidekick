package org.scala.sidekick

import org.gjt.sp.jedit. {View, EBMessage, EBPlugin}
import scalariform.formatter.preferences.FormattingPreferences
import scalariform.formatter.ScalaFormatter
import scalariform.parser.ScalaParserException

object ScalaSidekickPlugin {
  val NAME = "ScalaSidekickPlugin"

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
  }
}

class ScalaSidekickPlugin extends EBPlugin {
  override def handleMessage(message: EBMessage) {}
  override def start {}
}