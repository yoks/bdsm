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

  when(Idle, stateTimeout = 100 millis)(FSM.NullFunction)
  when(Preparing, stateTimeout = 1 second)(FSM.NullFunction)
  when(Dialing)(FSM.NullFunction)
  when(Talking, stateTimeout = talkTime millis)(FSM.NullFunction)

  whenUnhandled {
    case Event(s, d) =>
      log.warning("Unhandled AgentFSM Event({}, {}) in State {}", s, d, stateName)
      stay()
  }

  onTransition {
    case x -> y => log.info(s"Agent $phoneNumber goes into $y State, with Data - [$stateData]")
  }

  initialize()
}
