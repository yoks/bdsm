package pw.anisimov.pw.anisimov.connection

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import pw.anisimov.AgentFSM.{Busy, Hang, Accepted, PhoneCall}
import pw.anisimov.connection.DirectConnection
import pw.anisimov.connection.DirectConnection.Active

class DirectConnectionSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers
with BeforeAndAfterAll with BeforeAndAfter with ImplicitSender {
  def this() = this(ActorSystem("DirectConnectionSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "DirectConnection Connection" must {
    "must correctly connect Call" in {
      val fsm = TestFSMRef(new DirectConnection(self, self))
      watch(fsm) // Unsafe?
      fsm.stateName should be (DirectConnection.Dialing)
      expectMsg(PhoneCall(self))
      fsm ! Accepted
      fsm.stateName should be (Active)
      expectMsg(Accepted)
      fsm ! Hang

      // Each party receive Hang
      expectMsg(Hang)
      expectMsg(Hang)

      expectTerminated(fsm)
    }

    "must correctly reply if Busy" in {
      val fsm = TestFSMRef(new DirectConnection(self, self))
      watch(fsm) // Unsafe?
      fsm.stateName should be (DirectConnection.Dialing)
      expectMsg(PhoneCall(self))
      fsm ! Busy
      expectMsg(Busy)
      expectTerminated(fsm)
    }
  }

}
