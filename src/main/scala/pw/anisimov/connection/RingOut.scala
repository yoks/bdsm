package pw.anisimov.connection

import akka.actor.{Terminated, ActorRef, Props, FSM}
import RingOut._
import pw.anisimov.AgentFSM._
import pw.anisimov.connection.RingOut.Data
import pw.anisimov.connection.RingOut.Empty
import pw.anisimov.connection.RingOut.State
import scala.concurrent.duration._
import scala.language.postfixOps

object RingOut {
  def props(from: ActorRef, to: ActorRef) = Props(classOf[RingOut], from, to)

  sealed trait State
  case object DialingFrom extends State
  case object DialingTo extends State
  case object Active extends State

  sealed trait Data
  case object Empty extends Data
}

class RingOut(from: ActorRef, to: ActorRef) extends FSM[State, Data] {
  startWith(DialingFrom, Empty)

  override def preStart(): Unit = {
    from ! PhoneCall(to)
    context.watch(to)
    context.watch(from)
  }

  when(DialingFrom, stateTimeout = 1 second) {
    case Event(StateTimeout | Busy, _) =>
      stop()
    case Event(Accepted, _) =>
      to ! PhoneCall(from)
      goto(DialingTo)
  }

  when(DialingTo, stateTimeout = 1 second) {
    case Event(StateTimeout | Busy, _) =>
      from ! Busy
      stop()
    case Event(Accepted, _) =>
      from ! Accepted
      goto(Active)
  }

  when(Active) {
    case Event(Hang, _) =>
      from ! Hang
      to ! Hang
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