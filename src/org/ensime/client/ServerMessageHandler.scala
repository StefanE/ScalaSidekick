package org.ensime.client

import actors.Actor
import org.ensime.protocol.message._

object ServerMessageHandler extends Actor {
  def act() {
    loop{
      react{
        case info:ConnectionInfo => {
          println("INFOR:" + info)
        }
        case CompilerReady => {
          println("Compiler Ready")
          //ClientSender ! org.ensime.protocol.message.TypeAtPoint("D:/ensime/src/main/scala/org/ensime/protocol/SwankProtocol.scala",1302,0)
        }
        case result:TypeCheckResult => {
          println(result)
          //ClientSender ! org.ensime.protocol.message.ScopeCompletion("D:/ensime/src/main/scala/org/ensime/protocol/swankprotocol.scala", 1365, "", 0)
          ClientSender ! OrganizeImports("D:\\ensime\\src\\main\\scala\\org\\ensime\\util\\Note.scala",1,1,1318)
        }
        case bgMsg:BackgroundMessage => {
          println("bgMSG:" + bgMsg)
        }
        case Boolean => {
          println("ok")
        }
        case other => println("WTF" + other)
      }
    }
  }

  start
}