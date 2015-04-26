package pw.anisimov

import _root_.pw.anisimov.AgentFSM.Call
import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

class TelephonyServerSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers
with BeforeAndAfterAll with BeforeAndAfter with ImplicitSender {
  def this() = this(ActorSystem("TelephonyServerSpec"))

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "TelephonyServer" must {
    "must correctly initiate connection Call" in {
      val telephonyServer = system.actorOf(TelephonyServer.props())
      telephonyServer ! Call(Props(classOf[DummyConnection], self))
      expectMsg("Ping")
    }
  }
}

class DummyConnection(ref: ActorRef) extends Actor {
  override def preStart(): Unit = {
    ref ! "Ping"
  }

  override def receive: Receive = {
    case _ =>
  }
}
