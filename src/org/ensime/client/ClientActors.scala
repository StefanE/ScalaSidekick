package org.ensime.client

import actors.Actor
import org.ensime.util.WireFormat
import org.ensime.server.ServerReceiver

object ClientReceiver extends Actor {
  def act() {
    loop {
      react {
        case msg: WireFormat => {
          println("###Receiving:"+msg)
          ServerMessageHandler ! msg
        }
        case other => println("WTF:"+other)
      }
    }
  }
}

object ClientSender extends Actor {
  def act() {
    loop {
      react {
        case msg: WireFormat => {
          println("###SENDING:"+msg)
          ServerReceiver ! msg
        }
        case other => println("ERROR at ClientSender")
      }
    }
  }
}