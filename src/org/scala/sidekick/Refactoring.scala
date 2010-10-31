package org.scala.sidekick

import tools.refactoring.analysis.GlobalIndexes
import tools.refactoring.util.CompilerProvider
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.gjt.sp.jedit. {View, Buffer}
import tools.refactoring.implementations. {OrganizeImports, Rename}
import java.io.File
import org.ensime.protocol.message.{TypecheckFile, OrganizeImports}
import org.ensime.client.{Global, ClientSender}

object Refactoring {

  def rename(editor: JEditTextArea, view: View) {
    setCurrent(editor,view)
    val buffer = view.getBuffer
    val length = buffer.getLength
    val codeText = buffer.getText(0, length)

    val refactoring =
      new Rename with CompilerProvider with GlobalIndexes {
        val ast = treeFrom(codeText)
        val index = GlobalIndex(ast)
      }

    val selected = editor.getSelection(0)
    val selection: refactoring.Selection = {
      val file = refactoring.ast.pos.source.file
      val from = selected.getStart
      val to = selected.getEnd
      new refactoring.FileSelection(file, from, to)
    }

    val preparationResult = refactoring.prepare(selection) match {
      case Left(refactoring.PreparationError(error)) => {println(error);  () }
      case Right(r) => r
    }
  }


  def organizeImports(editor: JEditTextArea, view:View) {
    setCurrent(editor,view)
    val buffer = view.getBuffer
    val path = buffer.getPath
    val file = new File(buffer.getPath)
    val end = editor.getBufferLength
    var msgId = ScalaSidekickPlugin.msgCounter
    val procId = ScalaSidekickPlugin.procCounter
    //TODO: Workaround?
    ClientSender ! TypecheckFile(path,msgId)
    msgId = ScalaSidekickPlugin.msgCounter
    Thread.sleep(100)
    ClientSender ! OrganizeImports(path,procId,msgId,1,end)
  }

  private def setCurrent(editor: JEditTextArea, view:View) {
    Global.currentView = view
    Global.currentBuffer = view.getBuffer
  }
}