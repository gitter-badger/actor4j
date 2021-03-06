package actor4j.benchmark.akka.hub;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import static actor4j.benchmark.akka.hub.ActorMessageTag.*;

import actor4j.benchmark.akka.ActorMessage;

public class Client extends UntypedActor {
    protected ActorRef actor;
    
    protected long initalMessages;
    
    public Client(ActorRef actor) {
    	this.actor = actor;
    	
    	initalMessages = context().system().settings().config().getInt("my-dispatcher.throughput");
    }
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof ActorMessage) {
			if (((ActorMessage)message).tag==MSG.ordinal())
				actor.tell(message, getSelf());
			else if (((ActorMessage)message).tag==RUN.ordinal())
	            for (int i=0; i<initalMessages; i++)
	            	actor.tell(new ActorMessage(MSG), getSelf());
	    }
	}
}
