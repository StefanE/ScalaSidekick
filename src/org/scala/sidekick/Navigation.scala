package org.scala.sidekick

import collection.JavaConversions
import projectviewer.ProjectViewer
import io.Source
import projectviewer.vpt.VPTNode
import java.io._
import org.gjt.sp.jedit.textarea.JEditTextArea
import org.ensime.protocol.message.SymbolAtPoint
import org.scala.sidekick.ScalaSidekickPlugin._
import org.ensime.client.{Global, ClientSender}
import org.gjt.sp.jedit.{GUIUtilities, Buffer, jEdit, View}

object Navigation {

  private val OBJECT = "object"
  private val CLASS = "class"
  private val CASECLASS = "case class"
  private val TRAIT = "trait"

  private val INDEXFOLDER = "ScalaSidekick"
  private val INDEXFILENAME = "Sidekick.idx"

  var projectNodes = Iterable[VPTNode]()
  var indexLoaded = false
  var index = List[IndexEntry]()

  def navigateTo(view: View) {
    createIndex(view)

    val request = GUIUtilities.input(null,"info.goto",null)
    val elemList = index.filter(e => e.name == request)
    if(!elemList.isEmpty) {
      val goto = elemList(0)
      val buffer = jEdit.openFile(view,goto.path)
      try {
        val offset = view.getTextArea.getLineStartOffset(goto.line-1)
        view.getTextArea.setCaretPosition(offset)
      }
      catch {
        case e:NullPointerException => println("NullPointer at GotoDefinition")
      }
    }
    else {
      GUIUtilities.message(null,"info.goto.unknown",null)
    }

  }
  
  def gotoDefinition(textArea: JEditTextArea, view: View) {
    setCurrent(textArea,view)
    val buffer = view.getBuffer
    val path = buffer.getPath
    val caret = textArea.getCaretPosition()
    val msgID = msgCounter()
    
    Global.actions += msgID -> {(list:List[String]) => {
      //Faa en liste med to elementer prefix og path til fil, gaa hen til den
       val path = list(0)
       val offset = list(1).toInt
       val buffer = jEdit.openFile(view,path)
      try {
        view.getTextArea.setCaretPosition(offset)
      }
      catch {
        case e:NullPointerException => println("NullPointer at GotoDefinition")
      }

    } }
    
    ClientSender ! SymbolAtPoint(path,caret,msgID)
    
  }

  def createIndex(view: View) {
    val start = System.currentTimeMillis
    val project = ProjectViewer.getActiveProject(view)
    val rootPath = project.getRootPath
    projectNodes = JavaConversions.asIterable(project.getOpenableNodes)
    val scalaNodes = projectNodes.filter(_.getNodePath.endsWith("scala"))

    for (node <- scalaNodes) {
      //TODO: This binding maybe don't always work!?
      val lines = Source.fromFile(node.getNodePath)("ISO-8859-1").getLines
      var lineCounter = 0
      for (line <- lines) {
        var name = ""
        var tp = ""
        lineCounter += 1
        if (line.startsWith(OBJECT)) {
          name = line.split(' ')(1).split('{')(0)
          tp = OBJECT
        } else if (line.startsWith(CLASS)) {
          name = line.split(' ')(1).split('(')(0)
          tp = CLASS
        } else if (line.startsWith(CASECLASS)) {
          name = line.split(' ')(2).split('(')(0)
          tp = CASECLASS
        } else if (line.startsWith(TRAIT)) {
          name = line.split(' ')(1).split('{')(0)
          tp = TRAIT
        }
        if (name != "") index ::= IndexEntry(name, tp, node.getNodePath, lineCounter)
      }
    }
    val end = System.currentTimeMillis
    //Change it to jEdits info systems
    saveIndex()
    indexLoaded = true
    println("###:Created index in:" + (end - start))
  }

  private def saveIndex() {
    val homeDir = jEdit.getSettingsDirectory
    var dirName = homeDir + File.separator + INDEXFOLDER
    var indexdir = new File(dirName)
    if (!indexdir.exists)
      indexdir.mkdirs

    var f =  new File(dirName + File.separator + INDEXFILENAME)
    if (f.exists && f.canWrite) f.delete
    f.createNewFile

    val writer =  new BufferedWriter(new FileWriter(dirName + File.separator + INDEXFILENAME))

    for (node <- index) {
      println("TEST:" + node)
      writer.write(node + "\\n")
      writer.flush()

    }

    writer.close

    //Look into SIndex to see how I save on disk there
  }

  def loadIndex() {

    val homeDir = jEdit.getSettingsDirectory
    var dirName = homeDir + File.separator + INDEXFOLDER

    var f = new File(dirName + File.separator + INDEXFILENAME)
    if (f.exists && f.canRead) {
      val reader =  new BufferedReader(new FileReader(dirName + File.separator + INDEXFILENAME))

      for (node <- index) {
        println("TEST:" + node)
        val parts = reader.readLine().split(';')
        index ::= IndexEntry(parts(0),parts(1),parts(2),parts(3).toInt)
      }

      reader.close
    }
  }

  private def getIndexPath() {
    //Later remove duplicate code
  }

  private def setCurrent(editor: JEditTextArea, view:View) {
    Global.currentView = view
    Global.currentBuffer = view.getBuffer
  }
}

case class IndexEntry(name: String, tp: String, path: String, line: Int) {
  override def toString() = {
    name + ";" + tp + ";" + path + ";" + line
  }
}