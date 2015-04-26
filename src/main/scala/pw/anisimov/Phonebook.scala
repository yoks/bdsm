package pw.anisimov

import akka.actor.{Props, Terminated, ActorRef, Actor}

import scala.util.Random

class Phonebook extends Actor{
  var agents: Set[ActorRef] = Set()
  val random = new Random()

  override def receive: Receive = {
    case ref: ActorRef =>
      context.watch(ref)
      agents = agents + ref
    case Terminated(ref) =>
      agents = agents - ref
    case msg =>
      random.shuffle(agents.toList).head.forward(msg)
  }
}

object Phonebook {
  def props() = Props[Phonebook]
}