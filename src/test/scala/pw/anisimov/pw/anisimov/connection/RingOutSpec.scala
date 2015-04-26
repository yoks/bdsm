package pw.anisimov.pw.anisimov.connection

import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import pw.anisimov.AgentFSM.{Busy, Hang, Accepted, PhoneCall}
import pw.anisimov.connection.RingOut
import RingOut._

class RingOutSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers
with BeforeAndAfterAll with BeforeAndAfter with ImplicitSender {
  def this() = this(ActorSystem("RingOutSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "RingOut Connection" must {
    "must correctly connect Call" in {
      val fsm = TestFSMRef(new RingOut(self, self))
      watch(fsm) // Unsafe?
      fsm.stateName should be (DialingFrom)
      expectMsg(PhoneCall(self))
      fsm ! Accepted
      fsm.stateName should be (DialingTo)
      expectMsg(PhoneCall(self))
      fsm ! Accepted
      expectMsg(Accepted)

      fsm.stateName should be (Active)
      fsm ! Hang

      // Each party receive Hang
      expectMsg(Hang)
      expectMsg(Hang)

      expectTerminated(fsm)
    }

    "must correctly reply if to Busy" in {
      val fsm = TestFSMRef(new RingOut(self, self))
      watch(fsm) // Unsafe?
      fsm.stateName should be (DialingFrom)
      expectMsg(PhoneCall(self))
      fsm ! Accepted
      fsm.stateName should be (DialingTo)
      expectMsg(PhoneCall(self))
      fsm ! Busy
      expectMsg(Busy)

      expectTerminated(fsm)
    }

    "must correctly die if from Busy" in {
      val fsm = TestFSMRef(new RingOut(self, self))
      watch(fsm) // Unsafe?
      fsm.stateName should be (DialingFrom)
      expectMsg(PhoneCall(self))
      fsm ! Busy
      expectTerminated(fsm)
    }
  }

}
