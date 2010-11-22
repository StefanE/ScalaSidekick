package org.scala.sidekick

//{{{ Imports

import console.Console
import procshell.ProcessShell.ConsoleState
import console.Output
import java.util.HashMap
import java.util.Hashtable
import javax.swing.text.AttributeSet
import org.gjt.sp.jedit.Buffer
import org.gjt.sp.jedit.ServiceManager
import org.gjt.sp.jedit.View
import org.gjt.sp.jedit.gui.DockableWindowManager
import org.gjt.sp.jedit.jEdit
import org.gjt.sp.jedit.textarea.TextArea
import org.gjt.sp.util.Log
import procshell.ProcessShell
import java.io._

class ScalaShell() extends ProcessShell("Scala") {

  private val prompt = "scala>"

  protected override def init(state: ConsoleState, str:String) {

    Log.log(Log.DEBUG, this, "Attempting to start Scala process");

    //val pb = new ProcessBuilder("c:/scala/bin/scala.bat");
    val pb = new ProcessBuilder("java","-cp",
      "c:/scala/lib/scala-library.jar;c:/scala/lib/scala-compiler.jar",
      "scala.tools.nsc.MainGenericRunner","-usejavacp");
    state.p = pb.start();
    Log.log(Log.DEBUG, this, "Scala started.");
  }

  /* Use this when expanding */
  def eval(console: Console, str: String) {
    send(console, "")
  }

  protected override def onRead(state: ConsoleState, str: String, output: Output) {
    if (str.indexOf("\n") != -1) {
      //str = str.substring(str.lastIndexOf("\n") + 1);
    }
    if (str.matches(prompt)) {
      state.waiting = false;
      output.commandDone();
    }
  }
}
