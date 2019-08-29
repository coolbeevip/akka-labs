package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import akka.persistence.fsm.AbstractPersistentFSM;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.Domain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.MessageDomain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.StartDomain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.StopDomain;
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
    startWith(WorkerState.IDLE, WorkerData.builder().actorId(persistenceId).build());

    when(WorkerState.IDLE,
        matchEvent(StartedEvent.class,
            (event, data) -> {
              LOG.info("{} Started", event.getActorId());
              StartDomain domainEvent = new StartDomain(event);
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
              MessageDomain domainEvent = new MessageDomain(event);
              return stay().applying(domainEvent);
            }

        )
    );

    when(WorkerState.ACTIVE,
        matchEvent(StoppedEvent.class,
            (event, data) -> {
              LOG.info("{} Stopped, size={}", event.getActorId(), message.size());
              StopDomain domainEvent = new StopDomain(event);
              return stop().applying(domainEvent);
            }

        )
    );
  }

  @Override
  public void onRecoveryCompleted() {
    LOG.info("onRecoveryCompleted: {} {}", stateName(), stateData().getActorId());
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
    LOG.info("apply {} {}",domainEvent.getEvent().getActorId(),domainEvent.getClass().getSimpleName());
    if(domainEvent instanceof StartDomain){
      currentData.setActorId(domainEvent.getEvent().getActorId());
    }else if(domainEvent instanceof MessageDomain){
      currentData.appendMessage(((MessageDomain)domainEvent).getEvent().getText());
    }else if(domainEvent instanceof StopDomain){
      currentData.setActorId(domainEvent.getEvent().getActorId());
    }
    return currentData;
  }
}
