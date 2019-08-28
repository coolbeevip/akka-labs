package coolbeevip.labs.akka.java.kafka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteSingletonActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  ActorRef workerRouter = getContext()
      .actorOf(FromConfig.getInstance().props(Props.create(WorkerActor.class)), "workerRouter");

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Message.class, msg -> {
          workerRouter.tell(new ConsistentHashableEnvelope(msg,msg.actorId), self());
        }).match(Stop.class, msg -> {
          workerRouter.tell(new ConsistentHashableEnvelope(msg,msg.actorId), self());
        })
        .build();
  }
}
