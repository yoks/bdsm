package pw.anisimov.connection

import akka.actor.{Terminated, ActorRef, Props, FSM}
import DirectConnection._
import pw.anisimov.AgentFSM.{Hang, PhoneCall, Accepted, Busy}
import scala.concurrent.duration._
import scala.language.postfixOps

object DirectConnection {
  def props(from: ActorRef, to: ActorRef) = Props(classOf[DirectConnection], from, to)

  sealed trait State
  case object Dialing extends State
  case object Active extends State

  sealed trait Data
  case object Empty extends Data
}

class DirectConnection(from: ActorRef, to: ActorRef) extends FSM[State, Data] {
  startWith(Dialing, Empty)

  override def preStart(): Unit = {
    to ! PhoneCall(from)
    context.watch(to)
    context.watch(from)
  }

  when(Dialing, stateTimeout = 1 second) {
    case Event(StateTimeout | Busy, _) =>
      from ! Busy
      stop()
    case Event(Accepted, _) =>
      from ! Accepted
      goto(Active)
  }

  when(Active) {
    case Event(Hang, _) =>
      if (from == sender()) to ! Hang else from ! Hang
      stop()
  }

  whenUnhandled {
    case Event(Terminated(ref), _) =>
      if (to == ref) from ! Hang else to ! Hang
      stop()
    case Event(s, d) =>
      log.warning("Unhandled DirectConnection Event({}, {})", s, d)
      stay()
  }

  initialize()
}