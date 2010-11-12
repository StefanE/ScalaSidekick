package org.scala.sidekick

import scalariform.parser.ScalaParserException
import scalariform.formatter.ScalaFormatter
import scalariform.formatter.preferences.FormattingPreferences
import org.ensime.protocol.message.ReformatFile
import org.ensime.client.{Global, ClientSender}
import org.scala.sidekick.ScalaSidekickPlugin._
import org.gjt.sp.jedit.{GUIUtilities, View}

object Reformat {

  def format(view: View) {
    if (view.getBuffer.isDirty)
      GUIUtilities.message(null, "info.save", null)
    else {
      if (Initialized(view)) {
        setCurrent(view)
        val path = view.getBuffer.getPath
        val msgID = ScalaSidekickPlugin.msgCounter()
        Global.actions += msgID -> {
          (_: List[String]) => {

            view.getBuffer.reload(view)

          }
        }
        ClientSender ! ReformatFile(path, msgID)
      }
    }


  }

  private def setCurrent(view: View) {
    Global.currentView = view
    Global.currentBuffer = view.getBuffer
  }
}