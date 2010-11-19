package org.ensime.client

import org.gjt.sp.jedit.{View, Buffer}
import scala.collection.mutable.HashMap

object Global {
  var currentView: View = _
  var currentBuffer:Buffer = _
  var initialized = false
  val actions: HashMap[Int,Function1[List[String],Unit]] = HashMap()
  var typeCheck = false
}