package coolbeevip.labs.akka.java.cluster.pubsub.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TopicMessages.Message;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeacherActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Message.class, message -> {
          LOG.info("老师发布作业 " + message + self());
          mediator.tell(new DistributedPubSubMediator.Publish(message.getCourse().name(), message.getTask()), getSelf());
        })
        .build();
  }
}
