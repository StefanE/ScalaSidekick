package org.scala.sidekick

import org.gjt.sp.jedit.textarea.JEditTextArea
import scala.collection.JavaConversions
import tools.refactoring.util.{CompilerInstance, TreeCreationMethods, CompilerProvider}
import org.ensime.server.Server
import org.gjt.sp.jedit.{GUIUtilities, View, EBMessage, EBPlugin}
import org.ensime.client.{Global, ClientReceiver, ClientSender}
import org.gjt.sp.jedit.gui.CompletionPopup
import org.gjt.sp.jedit.gui.CompletionPopup.Candidates
import javax.swing.{JList, DefaultListCellRenderer}
import java.awt.Point
import org.ensime.protocol.message._
import projectviewer.{ProjectManager, ProjectViewer}
import projectviewer.event.ProjectUpdate
import projectviewer.vpt.{VPTFile, VPTNode}
import org.gjt.sp.jedit.msg.VFSUpdate
import errorlist.{ErrorSource, DefaultErrorSource}

object ScalaSidekickPlugin {
  val NAME = "ScalaSidekickPlugin"

  private var PROCID = 0
  private var MSGID = 0

  def procCounter() = {
    PROCID += 1;
    PROCID
  }

  def msgCounter() = {
    MSGID += 1;
    MSGID
  }

  def initProject(view: View) {
    var project = ProjectViewer.getActiveProject(view)
    if (project == null) {
      GUIUtilities.message(null, "error.ProjectViewer", null)
      return
    }
    var projectPath = project.getRootPath
    Global.typeCheck = true
    if (Global.initialized) {
      Global.initialized = false
      clearErrors
      ClientSender ! InitProject("c:/Users/Stefan/Desktop/emacs-23.2/dist", "", "sbt", projectPath, msgCounter())
      GUIUtilities.message(null, "info.restarting", null)
    }
    else {
      ClientReceiver.start
      ClientSender.start
      Server

      projectPath = GUIUtilities.input(null, "info.init.confirm", null, projectPath)

      ClientSender ! GetConnectionInfo(0)
      //ClientSender ! InitProject("c:/Users/Stefan/Desktop/emacs-23.2/dist", "", "sbt", projectPath, msgCounter())
      ClientSender ! InitProject("", "", "sbt", projectPath, msgCounter())

      Navigation.createIndex(view)
    }
  }

  def format(view: View) {
    if (Initialized(view))
      Reformat.format(view)
  }

  def getType(textArea: JEditTextArea, view: View) {
    if (Initialized(view)) {
      CodeAssist.getType(textArea, view)
    }
  }

  def complete(textArea: JEditTextArea, view: View) {
    if (Initialized(view))
      CodeAssist.complete(textArea, view)
  }

  def rename(textArea: JEditTextArea, view: View) {
    if (Initialized(view))
      Refactoring.rename(textArea, view)
  }

  def organizeImports(textArea: JEditTextArea, view: View) {
    if (Initialized(view))
      Refactoring.organizeImports(textArea, view)
  }

  def navigate(view: View) = {
    if (Initialized(view))
      Navigation.navigateTo(view)
  }

  def toDefinition(textArea: JEditTextArea, view: View) {
    if (Initialized(view))
      Navigation.gotoDefinition(textArea, view)
  }

  def typeCheckProject(textArea: JEditTextArea, view: View) {
    clearErrors()
    Global.typeCheck = true
    val msgID = msgCounter()

    Global.actions += msgID -> {
      any: Any => {
        any match {
          case result: TypeCheckResult => {
            val errors = new DefaultErrorSource("ProjectErrors")
            val notes = result.notes
            notes.foreach(note => {
              if (note.severity != 0) {
                val msg = note.msg
                val start = note.beg
                val length = note.end - start
                val line = note.line
                val path = note.file
                val severity =
                  if (note.severity == 2) ErrorSource.ERROR
                  else ErrorSource.WARNING
                errors.addError(severity, path, line - 1, start, 0, msg)
              }
            })
            if (Global.typeCheck)
              ErrorSource.registerErrorSource(errors)

            Global.typeCheck = false
          }
        }
      }
    }

    ClientSender ! TypecheckAll(msgID)
  }

  def Initialized(view: View) = {
    if (Global.initialized) {
      true
    }
    else {
      val projectRoot = ProjectViewer.getActiveProject(view).getRootPath
      val arr = Array[AnyRef](projectRoot)
      GUIUtilities.error(null, "error.noInit", arr)
      false
    }
  }

  def clearErrors() {
    val arr = ErrorSource.getErrorSources
    arr.foreach(e => {
      ErrorSource.unregisterErrorSource(e)
    })
  }
}

class ScalaSidekickPlugin extends EBPlugin {
  override def handleMessage(message: EBMessage) {
    message match {
      case e: ProjectUpdate => {
        val addedFiles = e.getAddedFiles
        if (addedFiles != null) {
          val list = JavaConversions.asIterable(addedFiles)
          list.foreach(e => {
            val path = e.getFile.getPath
            if (path.endsWith(".scala"))
              ClientSender ! TypecheckFile(path, ScalaSidekickPlugin.msgCounter())
          })
        }
      }
      case e: VFSUpdate => {
        /*  TODO: Steals focus from completion */
        val path = e.getPath
        if (path.endsWith(".scala") && Navigation.index.exists(_.path.equalsIgnoreCase(path))) {
          //ScalaSidekickPlugin.clearErrors()
          ClientSender ! TypecheckFile(path, ScalaSidekickPlugin.msgCounter())
        }

      }
      case other => println(other)
    }
  }

  override def start {
    Navigation.loadIndex()
  }

  override def stop {
    //Should stop scala shell?
  }
}

