package pw.anisimov

import akka.actor.{ActorLogging, Actor, Props}
import AgentFSM.Call

object TelephonyServer {
  def props() = Props[TelephonyServer]
}

class TelephonyServer extends Actor with ActorLogging {
  override def receive: Receive = {
    case Call(connection) =>
      context.actorOf(connection)
    case  msg =>
      log.error(s"Unhandled message ${msg.getClass}")
      log.info("{} message", msg)
  }
}
