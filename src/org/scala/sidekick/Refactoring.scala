package org.scala.sidekick

import tools.refactoring.analysis.GlobalIndexes
import tools.refactoring.util.CompilerProvider
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.gjt.sp.jedit. {View, Buffer}
import tools.refactoring.implementations. {OrganizeImports, Rename}
import tools.refactoring.common.Change

object Refactoring {

  def rename(textArea: JEditTextArea, view: View) {
    val buffer = view.getBuffer
    val length = buffer.getLength
    val codeText = buffer.getText(0, length)

    val refactoring =
      new Rename with CompilerProvider with GlobalIndexes {
        val ast = treeFrom(codeText)
        val index = GlobalIndex(ast)
      }

    val selected = textArea.getSelection(0)
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

  def organizeImports(editor: JEditTextArea) {
    val src = editor.getText
    val refactoring = new OrganizeImports with CompilerProvider{
      val ast = treeFrom(src)
    }

    val selection = refactoring.TreeSelection(refactoring.ast)
    val changes =
      refactoring.perform(selection, new refactoring.PreparationResult, new refactoring.RefactoringParameters) match {
      case Left(refactoring.RefactoringError(error)) =>
        println(error)
        return
      case Right(r) => r
    }

    editor setText Change.applyChanges(changes, src)
  }

  def organizeImports(src: String) {
    val refactoring = new OrganizeImports with CompilerProvider{
      val ast = treeFrom(src)
    }

    val selection = refactoring.TreeSelection(refactoring.ast)
    val changes =
      refactoring.perform(selection, new refactoring.PreparationResult, new refactoring.RefactoringParameters) match {
      case Left(refactoring.RefactoringError(error)) =>
        println(error)
        return
      case Right(r) => r
    }

  }
}