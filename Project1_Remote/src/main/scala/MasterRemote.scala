import akka.actor.Actor
import akka.actor.Props
import akka.routing.RoundRobinRouter
import akka.actor.ActorRef

class Master(noOfZeros: Int, stringLength: Int, noOfWorkers: Int, bitCoin: ActorRef, startTimer: Long, serverMaster: ActorRef) extends Actor{

	var listOfCoins: Map[String,String] = Map()
    var noOfCoins: Int = _
    var noWorkerDone: Int = 0
    
    val worker = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(noOfWorkers)), name = "worker")
	
	def receive = {
	   case BitCoin.FindBitCoin => 
	     println()
	     println("Bitcoin mining started...")
	     println()
	     for (i <- 1 to noOfWorkers) worker ! BitCoin.StartBitMining(stringLength, noOfZeros, startTimer)
	     
	   case BitCoin.ReturnBitCoin(listByWorker) =>
	      serverMaster ! BitCoin.ReturnBitCoin(listByWorker)
	      
	      
	   case BitCoin.DoneMining =>
	     noWorkerDone+= 1
	     if(noWorkerDone == noOfWorkers){
	       bitCoin ! BitCoin.DoneMining
	       context.stop(self)
	       context.system.shutdown()
	     }
	       
	}
}
