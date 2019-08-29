package coolbeevip.labs.akka.java.kafka.actor;

import akka.persistence.fsm.AbstractPersistentFSM;
import coolbeevip.labs.akka.java.kafka.actor.command.Domain;
import coolbeevip.labs.akka.java.kafka.actor.command.Start;
import coolbeevip.labs.akka.java.kafka.actor.event.EventEnded;
import coolbeevip.labs.akka.java.kafka.actor.event.EventMessage;
import coolbeevip.labs.akka.java.kafka.actor.event.EventStarted;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsmActor extends
    AbstractPersistentFSM<FsmState, FsmData, Domain> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<String> message = new ArrayList<>();
  private String persistenceId;

  public FsmActor() {
    persistenceId = getSelf().path().name();
    startWith(FsmState.IDLE, FsmData.builder().build());

    when(FsmState.IDLE,
        matchEvent(EventStarted.class,
            (event, data) -> {
              Start domainEvent = new Start();
              return goTo(FsmState.ACTIVE)
                  .applying(domainEvent);
            }

        )
    );

    when(FsmState.ACTIVE,
        matchEvent(EventMessage.class,
            (event, data) -> {
              message.add(event.getText());
              return stay();
            }

        )
    );

    when(FsmState.ACTIVE,
        matchEvent(EventEnded.class,
            (event, data) -> {
              LOG.info("{} size={}",getSelf(),message.size());
              return stop();
            }

        )
    );
  }

  @Override
  public Class domainEventClass() {
    return Domain.class;
  }


  @Override
  public String persistenceId() {
    return persistenceId;
  }

  @Override
  public FsmData applyEvent(Domain domainEvent, FsmData currentData) {
    return null;
  }
}
