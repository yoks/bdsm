package pw.anisimov

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import AgentFSM._

class AgentFSMSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers
with BeforeAndAfterAll with BeforeAndAfter with ImplicitSender {
  def this() = this(ActorSystem("AgentFSMSpec"))

  val AGENT_NUMBER = "79114445500"

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }


  "AgentFSM" must {
    "init in Idle state, and ask for Phonebook" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))

      fsm.stateName should be(AgentFSM.Idle)
      fsm.isStateTimerActive should be(true)
    }

    "answer identity in any state" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))

      fsm.stateName should be(AgentFSM.Idle)
      fsm ! Identity
      expectMsg(AgentIdentity(AGENT_NUMBER))

      fsm.setState(AgentFSM.Preparing)
      fsm.stateName should be(AgentFSM.Preparing)
      fsm ! Identity
      expectMsg(AgentIdentity(AGENT_NUMBER))

      fsm.setState(AgentFSM.Dialing)
      fsm.stateName should be(AgentFSM.Dialing)
      fsm ! Identity
      expectMsg(AgentIdentity(AGENT_NUMBER))

      fsm.setState(AgentFSM.Talking)
      fsm.stateName should be(AgentFSM.Talking)
      fsm ! Identity
      expectMsg(AgentIdentity(AGENT_NUMBER))
    }

    "be able to receive messages in Idle and Preparing state" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))

      fsm.stateName should be(AgentFSM.Idle)
      fsm ! PhoneCall(self)
      fsm.stateName should be(AgentFSM.Talking)

      fsm.setState(AgentFSM.Preparing)
      fsm.stateName should be(AgentFSM.Preparing)
      fsm ! PhoneCall(self)
      fsm.stateName should be(AgentFSM.Talking)
    }

    "reply Busy if already talking" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))
      fsm.setState(AgentFSM.Talking)
      fsm.stateName should be(AgentFSM.Talking)
      fsm ! PhoneCall(self)
      expectMsg(Busy)
    }

    "initiate call if got callee from phonebook" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))
      fsm.setState(AgentFSM.Preparing)
      fsm ! AgentIdentity("18882001133")

      val ts = system.actorOf(Forwarder.props(self))
      expectMsgClass(classOf[Call])

      watch(ts)
      ts ! PoisonPill
      expectTerminated(ts)
    }

    "go to Idle if got Busy while dialing" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))
      fsm.setState(AgentFSM.Dialing)
      fsm ! Busy
      fsm.stateName should be(AgentFSM.Idle)
    }

    "accept phone call" in {
      val fsm = TestFSMRef(new AgentFSM(AGENT_NUMBER))
      fsm ! PhoneCall(self)
      fsm.stateName should be(AgentFSM.Talking)
      expectMsg(Accepted)
    }
  }
}