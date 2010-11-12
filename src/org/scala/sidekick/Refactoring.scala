package org.scala.sidekick

import tools.refactoring.analysis.GlobalIndexes
import tools.refactoring.util.CompilerProvider
import org.gjt.sp.jedit.textarea.JEditTextArea
import tools.refactoring.implementations.{OrganizeImports, Rename}
import java.io.File
import org.ensime.client.{Global, ClientSender}
import org.ensime.protocol.message.{Rename, TypecheckFile, OrganizeImports}
import org.gjt.sp.jedit.{GUIUtilities, View, Buffer}

object Refactoring {

  def rename(editor: JEditTextArea, view: View) {
    if (view.getBuffer.isDirty)
      GUIUtilities.message(null, "info.save", null)
    else {
      val newName = GUIUtilities.input(null, "info.rename", null)

      val buffer = view.getBuffer
      val path = buffer.getPath

      val select = editor.getSelection(0)
      val start = select.getStart
      val end = select.getEnd
      val procID = ScalaSidekickPlugin.procCounter
      val msgID = ScalaSidekickPlugin.msgCounter

      Global.actions += msgID -> {
        (_: List[String]) => {
          buffer.reload(view)
        }
      }

      ClientSender ! Rename(path, procID, msgID, start, end, newName)
    }
  }


  def organizeImports(editor: JEditTextArea, view: View) {
    if (view.getBuffer.isDirty)
      GUIUtilities.message(null, "info.save", null)
    else {
      val buffer = view.getBuffer
      val path = buffer.getPath
      val file = new File(buffer.getPath)
      val end = editor.getBufferLength
      var msgId = ScalaSidekickPlugin.msgCounter
      val procId = ScalaSidekickPlugin.procCounter

      Global.actions += msgId -> {
        (_: List[String]) => {
          view.getBuffer.reload(view)
        }
      }

      ClientSender ! OrganizeImports(path, procId, msgId, 1, end)
    }
  }

  private def setCurrent(editor: JEditTextArea, view: View) {
    Global.currentView = view
    Global.currentBuffer = view.getBuffer
  }
}