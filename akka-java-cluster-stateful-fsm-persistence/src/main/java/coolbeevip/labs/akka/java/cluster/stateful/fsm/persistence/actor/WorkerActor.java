package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import akka.persistence.fsm.AbstractPersistentFSM;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.Domain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.StartDomain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StoppedEvent;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.EventMessage;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StartedEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerActor extends
    AbstractPersistentFSM<WorkerState, WorkerData, Domain> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private List<String> message = new ArrayList<>();
  private String persistenceId;

  public WorkerActor() {
    persistenceId = getSelf().path().name();
    startWith(WorkerState.IDLE, WorkerData.builder().build());

    when(WorkerState.IDLE,
        matchEvent(StartedEvent.class,
            (event, data) -> {
              LOG.info("{} Started", event.getActorId());
              StartDomain domainEvent = new StartDomain();
              return goTo(WorkerState.ACTIVE)
                  .applying(domainEvent);
            }

        )
    );

    when(WorkerState.ACTIVE,
        matchEvent(EventMessage.class,
            (event, data) -> {
              LOG.info("{} Message {}", event.getActorId(), event.getText());
              message.add(event.getText());
              return stay();
            }

        )
    );

    when(WorkerState.ACTIVE,
        matchEvent(StoppedEvent.class,
            (event, data) -> {
              LOG.info("{} Stopped, size={}", event.getActorId(), message.size());
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
  public WorkerData applyEvent(Domain domainEvent, WorkerData currentData) {
    return null;
  }
}
