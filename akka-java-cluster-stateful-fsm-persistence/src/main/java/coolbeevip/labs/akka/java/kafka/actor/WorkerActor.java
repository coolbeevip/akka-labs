package coolbeevip.labs.akka.java.kafka.actor;

import akka.actor.AbstractActor;
import coolbeevip.labs.akka.java.kafka.actor.event.EventEnded;
import coolbeevip.labs.akka.java.kafka.actor.event.EventMessage;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  List<String> messages = new ArrayList<>();

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(EventMessage.class, msg -> {
          messages.add(msg.getText());
          LOG.info("{} 收到消息 {}, key {}, total {}", self(), msg.getText(), msg.getActorId(),messages.size());
        })
        .match(EventEnded.class, msg -> {
          getContext().stop(getSelf());
        })
        .build();
  }

  @Override
  public void preStart() throws Exception {
    super.preStart();
    LOG.info("{} 开始启动", getSelf());
  }

  @Override
  public void postStop() throws Exception {
    super.postStop();
    LOG.info("{} 已停止", getSelf());
  }

  @Override
  public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
    super.preRestart(reason,message);
    LOG.info("{} 开始重启", getSelf());
  }

  @Override
  public void postRestart(Throwable reason) throws Exception {
    super.postRestart(reason);
    LOG.info("{} 重启完毕", getSelf());
  }

}
