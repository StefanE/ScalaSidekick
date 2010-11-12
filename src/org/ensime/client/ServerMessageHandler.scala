package org.ensime.client

import actors.Actor
import org.ensime.protocol.message._
import org.ensime.server.RefactorEffect
import org.scala.sidekick.ScalaSidekickPlugin
import org.ensime.model.NamedTypeMemberInfoLight

object ServerMessageHandler extends Actor {
  def act() {
    loop{
      react{
        case info: ConnectionInfo => {
          println("INFOR:" + info)
        }
        case CompilerReady() => {
          Global.initialized = true
          println("Compiler Ready:" + Global.initialized)
        }
        case result: TypeCheckResult => {
          println(result)
        }
        case bgMsg: BackgroundMessage => {
          println("bgMSG:" + bgMsg)
        }
        case SymbolInfoLightMsg(value) => {
          println(value)
        }
        case RefactorResultMsg(value) => {
          println("RefactorResult:" + value.touched)
          Global.currentBuffer.reload(Global.currentView)
          //Global.currentBuffer.autosave()
        }
        case RefactorEffectMsg(value) => handleRefactoring(value)
        case RefactorFailureMsg(value) => {
          println("Failure:" + value)
        }

        case Container(value, id) => {
          
          val action = Global.actions.remove(id).getOrElse(null)
          if (action != null) {
            value match {
              case IterableValues(values) => {
                val sList: List[String] = (for (member <- values) yield member.toWireString).toList
                action(sList)
              }
              case TypeInfoMsg(value) => {
                val sList = List(value.fullName)
                action(sList)
              }
              case SymbolInfoMsg(value) => {
                try {
                  val pos = value.declPos
                  val path = pos.source.path
                  val offset = pos.point
                  val sList = List(path,offset.toString)
                  action(sList)
                }
                catch {
                  case e:UnsupportedOperationException => println("Could not find declaration")
                }

              }
              case BooleanMsg(value) => {
                action(null)
              }
              case other => ()
            }
          }
        }
        case IterableValues(list) => {
          //Global.actions.remove(1)
        }
        case other => println("WTF:" + other)
      }
    }
  }

  private def handleRefactoring(effect: RefactorEffect) {
    effect.refactorType.toString match {
      case "'organizeImports" => {
        println("###Text:" + effect.changes.mkString)
        ClientSender ! ExecRefactoring("organizeImports", effect.procedureId, ScalaSidekickPlugin.msgCounter)
        println("###Exec")
      }
      case "'rename" => {
        println("###Text:" + effect.changes.mkString)
        ClientSender ! ExecRefactoring("rename", effect.procedureId, ScalaSidekickPlugin.msgCounter)
        println("###Exec")
      }
      case other => println("Uknown refactor:" + other)
    }
  }

  start
}