package org.scala.sidekick

import org.gjt.sp.jedit.View
import collection.JavaConversions
import projectviewer.ProjectViewer

object Navigation {
  def navigateTo(view:View) {

  }

  def createIndex(view:View) {
     val project = ProjectViewer.getActiveProject(view)
    val rootPath = project.getRootPath
    val nodes = JavaConversions.asIterable(project.getOpenableNodes)
    val scalaNodes = nodes.filter(_.getNodePath.endsWith(".scala"))
    //nodes.foreach(node => println("###:"+node.getNodePath))
  }
}