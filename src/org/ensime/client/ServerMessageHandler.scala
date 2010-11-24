package org.ensime.client

import actors.Actor
import org.ensime.protocol.message._
import org.ensime.server.RefactorEffect
import org.scala.sidekick.ScalaSidekickPlugin
import org.ensime.model.NamedTypeMemberInfoLight
import errorlist.{ErrorSource, DefaultErrorSource}
import org.gjt.sp.jedit.GUIUtilities

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
          GUIUtilities.message(null,"info.ready",null)
        }
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
          println(Global.typeCheck + "" + result)

          //First returns java errors, and then scala errors
          //TODO:
            Global.typeCheck = false
        }
        case bgMsg: BackgroundMessage => {
          println("bgMSG:" + bgMsg)
        }
        case SymbolInfoLightMsg(value) => {
          println(value)
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
                try
                {
                  val pos = value.declPos
                  val path = pos.source.path
                  val offset = pos.point
                  val sList = List(path, offset.toString)
                  action(sList)
                }
                catch {
                  case e: UnsupportedOperationException => println("Could not find declaration")
                }

              }
              case BooleanMsg(value) => {
                action(null)
              }
              case RefactorEffectMsg(value) => {
                val id = ScalaSidekickPlugin.msgCounter

                Global.actions += id -> {
                  (_: List[String]) => action(null)
                }
                handleRefactoring(value, id)
              }
              case RefactorResultMsg(value) => action(null)
              case RefactorFailureMsg(value) => {
                println("Failure:" + value)
              }
              case other => ()
            }
          }
        }
        case other => println("WTF:" + other)
      }
    }
  }

  private def handleRefactoring(effect: RefactorEffect, id: Int) {
    effect.refactorType.toString match {
      case "'organizeImports" => {
        println("###Text:" + effect.changes.mkString)
        ClientSender ! ExecRefactoring("organizeImports", effect.procedureId, id)
        println("###Exec")
      }
      case "'rename" => {
        println("###Text:" + effect.changes.mkString)
        ClientSender ! ExecRefactoring("rename", effect.procedureId, id)
        println("###Exec")
      }
      case other => println("Uknown refactor:" + other)
    }
  }

  start
}