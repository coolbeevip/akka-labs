package coolbeevip.labs.akka.java.cluster.pubsub.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudentActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public StudentActor(String course) {
    ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    LOG.info("学生 " + self() + " 订阅课程 " + course);
    mediator.tell(new DistributedPubSubMediator.Subscribe(course, getSelf()), getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(String.class, message -> LOG.info("学生 "+self()+" 收到消息 " + message))
        .match(DistributedPubSubMediator.SubscribeAck.class, msg -> LOG.info("学生" + self()+" 订阅成功"))
        .build();
  }
}
