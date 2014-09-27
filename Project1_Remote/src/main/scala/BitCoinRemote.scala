import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.util.control.Breaks
import scala.collection.mutable.ArrayBuffer
import com.typesafe.config.ConfigFactory
import akka.actor.AddressFromURIString
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.net.InetAddress
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

object MainProg extends App{
  
  override def main(args: Array[String]): Unit = {
	
	val hostIP = InetAddress.getLocalHost.getHostAddress()
	//println(hostIP)
	
  val config = ConfigFactory.parseString("""
    akka {
       actor {
           provider = "akka.remote.RemoteActorRefProvider"
             }
       remote {
           enabled-transports = ["akka.remote.netty.tcp"]
       netty.tcp {
           hostname = """ + hostIP + """
           port = 6801
                 }
             }
        }
   """)  

    val remoteSystem = ActorSystem("remoteSystem", ConfigFactory.load(config))
    
    val duration = Duration(100, SECONDS)
    val path: String =  "akka.tcp://serverSystem@" + args(0) + ":5150/user/bitcoin"

    val future = remoteSystem.actorSelection(path).resolveOne(duration)
   
    val bitcoinremote = remoteSystem.actorOf(Props( new BitCoinRemote(future, remoteSystem)), "bitcoinremote")

  }
}


object BitCoin {

  trait MessageType
  case object FindBitCoin extends MessageType
  case class StartBitMining(length: Int, noOfZeros: Int,start: Long) extends MessageType
  case class ReturnBitCoin(listOfCoins: Map[String,String]) extends MessageType
  case class FinalResult(listOfCoins: Map[String,String], noOfcoins: Int) extends MessageType
  case class StartWork(startTimer: Long, masterRef: ActorRef)
  case class ResultFromRemote(listOfCoins: Map[String, String])
  case object DoneMining extends MessageType
}
  
  class BitCoinRemote(future: scala.concurrent.Future[akka.actor.ActorRef], remoteSystem: ActorSystem) extends Actor {    
    import BitCoin._

    var server: ActorRef = _
    
    future.onComplete {
      case Success(value) => 
        value ! "Connection Complete"
        server = value
    
      case Failure(e) => e.printStackTrace
    }
     
    
  def receive = {
    
    case StartWork(startTimer, serverMaster) =>
     val master = remoteSystem.actorOf(Props(new Master(3, 10, 9, self, startTimer, serverMaster)), "Master")
     master ! FindBitCoin 
     
    case DoneMining =>
      println()
      println("Shutting down remote system")
      println()
      context.stop(self)
      //context.system.shutdown
      //System.exit(0)
   }
  
 }
