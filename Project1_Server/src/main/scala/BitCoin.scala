import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.util.control.Breaks
import scala.collection.mutable.ArrayBuffer
import com.typesafe.config.ConfigFactory
import java.net.InetAddress
import akka.actor.ActorRefFactory
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._


object MainProg extends App{
  
  override def main(args: Array[String]): Unit = {
   
    val noOfZeroes: Int = args(0).toInt
    //val noOfZeroes: Int = 3
	//println(noOfZeroes)
    
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
					port = 5150
				}
			}
		}
	""")  

	val serverSystem = ActorSystem("serverSystem", ConfigFactory.load(config))
	val bitcoin = serverSystem.actorOf(Props(new BitCoin(serverSystem, noOfZeroes)), "bitcoin")

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
  
  class BitCoin(serverSystem: ActorSystem, noOfZeroes: Int) extends Actor{
    import BitCoin._
    
    val startTimer: Long = System.currentTimeMillis
    
    val master = serverSystem.actorOf(Props(new Master(noOfZeroes, 10, 9, self, startTimer)), "Master")
    master ! FindBitCoin 
    
    
  def receive = {
      
    case "Connection Complete" =>
      sender ! StartWork(startTimer, master)
      
    case FinalResult(listOfCoins, noOfCoins) =>
      	
	    println("Bitcoins :")
	    println()
	    var countZero: Int = 0
	    var maxZero: Int = 0
	    var maxInput: String = ""
	    var maxOutput: String = ""
	    var finalList: Map[Int,String] = Map()
	    listOfCoins foreach {case (key, value) => 
	     var countZero: Int = 0
	     val loop = new Breaks;
	      	loop.breakable{for(i <- 0 to 63){
	      		if(value.charAt(i).equals('0')){
	      			countZero+=1
	      		}else{
	      			loop.break
	      		}
	      	}
	      }
	      if(maxZero < countZero){
	      	maxZero = countZero
	      	maxInput = key
	      	maxOutput = value
	      }
	      var str: String = key + '\t' + value
	      if(!finalList.contains(countZero)){
	        finalList+= countZero -> str
	      }
	    }
	    //var i: Int = 1
	    for(i <- 1 to maxZero) {
              if(finalList.contains(i))
   	 	println(finalList.apply(i))
   	    }
  
	    println()
  	    println("Total number of bitcoins generated : " + noOfCoins)
         println()
	    
	    context.stop(self)
	    context.system.shutdown()
	    System.exit(0)
   }    
  }
