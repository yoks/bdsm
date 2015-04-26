package pw.anisimov

import akka.actor.{Actor, Props}
import AgentFSM.Call

object TelephonyServer {
  def props() = Props[TelephonyServer]
}

class TelephonyServer extends Actor {
  override def receive: Receive = {
    case Call(connection) =>
      context.actorOf(connection)
  }
}
