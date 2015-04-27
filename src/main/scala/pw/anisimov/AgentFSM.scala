package pw.anisimov

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, FSM, Props}
import AgentFSM._
import com.typesafe.config.ConfigFactory
import pw.anisimov.connection.DirectConnection
import pw.anisimov.connection.RingOut
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.util.Random

object AgentFSM {
  def props(phoneNumber: String) = Props(classOf[AgentFSM], phoneNumber)

  sealed trait State
  case object Idle extends State
  case object Preparing extends State
  case object Dialing extends State
  case object Talking extends State

  sealed trait Data
  case object Empty extends Data
  case class CurrentCall(connection: ActorRef) extends Data

  case class PhoneCall(from: ActorRef)
  case class Call(callType: Props)
  case class AgentIdentity(phoneNumber: String)
  case object Busy
  case object Identity
  case object Accepted
  case object Hang
}

class AgentFSM(phoneNumber: String) extends FSM[State, Data] {
  val random = new Random()
  val phonebook = context.actorSelection("/user/phonebook")
  val telephonyServer = context.actorSelection("/user/telephonyServer")
  val talkTime = ConfigFactory.load().getDuration("bdsm.talk-time", TimeUnit.MILLISECONDS) + random.nextInt(1000)

  startWith(Idle, Empty)

  when(Idle, stateTimeout = 100 millis){
    case Event(PhoneCall(from), _) =>
      goto(Talking) replying Accepted using CurrentCall(sender())
    case Event(StateTimeout, _) =>
      phonebook.tell(Identity, self)
      goto(Preparing)
  }
  when(Preparing, stateTimeout = 1 second){
    case Event(PhoneCall(from), _) =>
      goto(Talking) replying Accepted using CurrentCall(sender())
    case Event(StateTimeout, _) =>
      goto(Idle)
    case Event(AgentIdentity(from), _) =>
      if(random.nextBoolean()) {
        telephonyServer.tell(Call(DirectConnection.props(self, sender())), self)
        goto(Dialing)
      } else {
        telephonyServer.tell(Call(RingOut.props(self, sender())), self)
        goto(Idle)
      }
  }
  when(Dialing){
    case Event(Busy, _) =>
      goto(Idle)
    case Event(Accepted, _) =>
      goto(Talking) using CurrentCall(sender())
    case Event(PhoneCall(from), _) =>
      stay() replying Busy

  }
  when(Talking, stateTimeout = talkTime millis){
    case Event(Hang, _) =>
      goto(Idle) using Empty
    case Event(StateTimeout, CurrentCall(connection)) =>
      connection ! Hang
      goto(Idle) using Empty
    case Event(PhoneCall(from), _) =>
      stay() replying Busy
  }

  whenUnhandled {
    case Event(Identity,_) =>
      stay() replying AgentIdentity(phoneNumber)

    case Event(s, d) =>
      log.warning("Unhandled AgentFSM Event({}, {}) in State {}", s, d, stateName)
      stay()
  }

  onTransition {
    case x -> y => log.info(s"Agent $phoneNumber goes into $y State")
  }

  initialize()
}