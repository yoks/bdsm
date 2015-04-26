package pw.anisimov

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, FSM, Props}
import AgentFSM._
import com.typesafe.config.ConfigFactory
import scala.language.postfixOps

object AgentFSM {
  def props(phoneNumber: String) = Props(classOf[AgentFSM], phoneNumber)

  sealed trait State
  case object Idle extends State
  case object Preparing extends State
  case object Dialing extends State
  case object Talking extends State

  sealed trait Data
  case object Empty extends Data
  case class Calling(call: Call) extends Data

  case class PhoneCall(from: ActorRef)
  case class Call(callType: Props)
  case class AgentIdentity(phoneNumber: String)
  case object Busy
  case object Identity
  case object Accepted
  case object Hang
}

class AgentFSM(phoneNumber: String) extends FSM[State, Data] {
  val phonebook = context.actorSelection("/user/phonebook")
  val telephonyServer = context.actorSelection("/user/telephonyServer")
  val talkTime = ConfigFactory.load().getDuration("bdsm.talk-time", TimeUnit.MILLISECONDS)

  startWith(Idle, Empty)

  when(Idle)(FSM.NullFunction)
  when(Preparing)(FSM.NullFunction)
  when(Dialing)(FSM.NullFunction)
  when(Talking)(FSM.NullFunction)

  whenUnhandled {
    case Event(s, d) =>
      log.warning("Unhandled AgentFSM Event({}, {})", s, d)
      stay()
  }

  initialize()
}
