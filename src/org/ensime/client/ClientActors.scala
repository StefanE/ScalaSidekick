package org.ensime.client

import actors.Actor
import org.ensime.util.WireFormat
import org.ensime.server.ServerReceiver

object ClientReceiver extends Actor {
  def act() {
    loop {
      react {
        case msg: WireFormat => ServerMessageHandler ! msg
        case other => println("WTF:"+other)
      }
    }
  }
}

object ClientSender extends Actor {
  def act() {
    loop {
      react {
        case msg: WireFormat => ServerReceiver ! msg
        case other => println("ERROR at ClientSender")
      }
    }
  }
}