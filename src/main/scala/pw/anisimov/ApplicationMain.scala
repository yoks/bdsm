package pw.anisimov

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ApplicationMain extends App {
  val system = ActorSystem("bdsm")

  val phonebook  = system.actorOf(Phonebook.props(), "phonebook")

  system.actorOf(TelephonyServer.props(), "telephonyServer")

  val agentsNum = ConfigFactory.load().getInt("bdsm.agents")

  for (number <- 2500000 until 2500000 + agentsNum) {
    val phoneNumber  = "1650" + number
    val ref = system.actorOf(AgentFSM.props(phoneNumber), phoneNumber)
    phonebook ! ref
  }

  println(s"$agentsNum agents created.")
}