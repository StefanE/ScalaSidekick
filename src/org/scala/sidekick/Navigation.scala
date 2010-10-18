package org.scala.sidekick

import org.gjt.sp.jedit.View
import collection.JavaConversions
import projectviewer.ProjectViewer
import io.Source

object Navigation{

  private val OBJECT = "object"
  private val CLASS = "class"
  private val CASECLASS = "case class"
  private val TRAIT = "trait"

  private var index = List[IndexEntry]()

  def navigateTo(view: View) {

  }

  def createIndex(view: View) {
    val start = System.currentTimeMillis
    val project = ProjectViewer.getActiveProject(view)
    val rootPath = project.getRootPath
    val nodes = JavaConversions.asIterable(project.getOpenableNodes)
    val scalaNodes = nodes.filter(_.getNodePath.endsWith("scala"))

    for (node <- scalaNodes) {
      //TODO: This binding could be dangerous!?
      val lines = Source.fromFile(node.getNodePath)("ISO-8859-1").getLines
      var lineCounter = 0
      for (line <- lines) {
        var name = ""
        var tp = ""
        lineCounter += 1
        if (line.startsWith(OBJECT)) {
          name = line.split(' ') (1).split('{') (0)
          tp = OBJECT
        }
        else if (line.startsWith(CLASS)) {
          name = line.split(' ') (1).split('(') (0)
          tp = CLASS
        }
        else if (line.startsWith(CASECLASS)) {
          name = line.split(' ') (2).split('(') (0)
          tp = CASECLASS
        }
        else if (line.startsWith(TRAIT)) {
          name = line.split(' ') (1).split('{') (0)
          tp = TRAIT
        }

        if(name != "") {
          index ::= IndexEntry(name, tp, node.getNodePath, lineCounter)
        }

      }
    }
    val end = System.currentTimeMillis
    //Change it to jEdits info systems
    println("###:Created index in:" + (end - start))
    println("###"+index)

  }

  private def saveIndex() {
    //Look into SIndex to see how I save on disk there
  }
}

case class IndexEntry(name:String, tp:String, path:String, line:Int)