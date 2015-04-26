package pw.anisimov

import akka.actor.{Actor, ActorRef, Props}

class Forwarder(ref: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg =>
      ref forward msg
  }
}

object Forwarder {
  def props(ref: ActorRef) = Props(classOf[Forwarder], ref)
}
