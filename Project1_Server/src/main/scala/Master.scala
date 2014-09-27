import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import akka.actor.ActorRef

class Master(noOfZeros: Int, stringLength: Int, noOfWorkers: Int, bitCoin: ActorRef, startTimer: Long) extends Actor{

	var listOfCoins: Map[String,String] = Map()
    var noOfCoins: Int = _
    var noWorerkDone: Int = 0
   
    val worker = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(noOfWorkers)), name = "worker")
	
	def receive = {
	  
	   case BitCoin.FindBitCoin => 
	     println()
	     println("Bitcoin mining started...")
	     println()
	     for (i <- 1 to noOfWorkers) worker ! BitCoin.StartBitMining(stringLength, noOfZeros, startTimer)
	     
	   case BitCoin.ReturnBitCoin(listByWorker) =>
	      listByWorker foreach {case (key, value) => listOfCoins+= key -> value 
	      noOfCoins+= 1}
	      
	      
	   case BitCoin.DoneMining =>
	     noWorerkDone+= 1
	     if(noWorerkDone == noOfWorkers){
	       bitCoin ! BitCoin.FinalResult(listOfCoins, noOfCoins)
	       //context.stop(self)
	     }
	      
	}
}
