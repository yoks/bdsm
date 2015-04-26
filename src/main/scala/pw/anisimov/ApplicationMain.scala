package pw.anisimov

import akka.actor.{ActorSystem, Props}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

object ApplicationMain extends App {
  val system = ActorSystem("bdsm")

  system.actorOf(FromConfig.getInstance().props(Props.empty), "phonebook")

  system.actorOf(TelephonyServer.props(), "telephonyServer")

  val agentsNum = ConfigFactory.load().getInt("bdsm.agents")

  for (number <- 2500000 until 2500000 + agentsNum) {
    val phoneNumber  = "1650" + number
    system.actorOf(AgentFSM.props(phoneNumber), phoneNumber)
  }

  println(s"$agentsNum agents created.")
}