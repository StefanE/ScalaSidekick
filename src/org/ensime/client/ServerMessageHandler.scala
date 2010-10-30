package org.ensime.client

import actors.Actor
import org.ensime.protocol.message._
import org.ensime.server.RefactorEffect
import org.scala.sidekick.ScalaSidekickPlugin

object ServerMessageHandler extends Actor {
  def act() {
    loop{
      react{
        case info:ConnectionInfo => {
          println("INFOR:" + info)
        }
        case CompilerReady => {
          println("Compiler Ready")
        }
        case result:TypeCheckResult => {
          println(result)
        }
        case bgMsg:BackgroundMessage => {
          println("bgMSG:" + bgMsg)
        }
        case Boolean => {
          println("ok")
        }
        case SymbolInfoLightMsg(value) => {
          println(value)
        }
        case RefactorResultMsg(value) => {
          println("RefactorResult:"+value.touched)
          Global.currentBuffer.reload(Global.currentView)
          Global.currentBuffer.autosave()
        }
        case RefactorEffectMsg(value) => handleRefactoring(value)
        case RefactorFailureMsg(value) => {
          println("Failure:"+value)
        }
        case other => println("WTF:" + other)
      }
    }
  }
  
  private def handleRefactoring(effect:RefactorEffect) {
    effect.refactorType.toString match {
      case "'organizeImports" => {
        println("###Text:"+effect.changes.mkString)
        ClientSender ! ExecRefactoring("organizeImports",effect.procedureId,ScalaSidekickPlugin.msgCounter)
        println("###Exec")
      }
      case other => println("Uknown refactor:"+other)
    }
  }

  start
}