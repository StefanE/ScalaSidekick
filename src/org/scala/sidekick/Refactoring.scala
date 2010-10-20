package org.scala.sidekick


import org.gjt.sp.jedit.textarea.JEditTextArea
import org.gjt.sp.jedit. {View, Buffer}
import org.ensime.protocol.message.OrganizeImports
import org.ensime.client.ClientSender


object Refactoring {

  def rename(textArea: JEditTextArea, view: View) {

  }

  def organizeImports(editor: JEditTextArea, view:View) {
    val path = view.getBuffer.getPath
    val end = editor.getBufferLength
    ClientSender ! OrganizeImports(path,1000,1,end)
  }
}