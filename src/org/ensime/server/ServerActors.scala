package org.ensime.server

import org.ensime.protocol._
import org.ensime.util.WireFormat
import scala.actors._

object Server {

  System.setProperty("actors.corePoolSize", "25")
  val protocol: Protocol = ScalaProtocol

  val project: Project = new Project(protocol)
  project.start
  ServerReceiver.start
  ServerSender.start

}

object ServerReceiver extends Actor {

  def act() {
    loop {
      react {
        case msg: WireFormat => Server.project ! IncomingMessageEvent(msg)
        case other => println("WTF:"+other)  
      }
    }
  }
}

object ServerSender extends Actor {

  Server.protocol.setOutputActor(this)
  
  def act() {
    loop {
      react {
        case OutgoingMessageEvent(value: WireFormat) => Server.protocol.writeMessage(value, null)
        case other => println("WTF:"+other)
      }
    }
  }
}