package coolbeevip.labs.akka.java.kafka.actor;

import akka.actor.AbstractActor;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Message.class, msg -> {
          LOG.info("{} 收到消息 {}, key {}", self(), msg.getText(), msg.getActorId());
        })
        .match(Stop.class, msg -> {
          postStop();
          LOG.info("{} 销毁", self());
        })
        .build();
  }
}
