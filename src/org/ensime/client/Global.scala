package org.ensime.client

import org.gjt.sp.jedit.{View, Buffer}

object Global {
  var currentView: View = _
  var currentBuffer:Buffer = _
  var initialized = false
  var relatedNumber = 0
}