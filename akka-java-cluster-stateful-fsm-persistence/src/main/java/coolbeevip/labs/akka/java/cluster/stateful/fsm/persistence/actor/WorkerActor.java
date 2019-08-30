package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import akka.actor.PoisonPill;
import akka.cluster.sharding.ShardRegion;
import akka.persistence.fsm.AbstractPersistentFSM;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.Domain;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain.MessageDomain;
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
    LOG.debug("{} init", getSelf());
    persistenceId = getSelf().path().name();
    startWith(WorkerState.IDLE, WorkerData.builder().actorId(persistenceId).build());

    when(WorkerState.IDLE,
        matchEvent(StartedEvent.class,
            (event, data) -> {
              LOG.info("{} receive Started", getSelf());
              StartDomain domainEvent = new StartDomain(event);
              return goTo(WorkerState.ACTIVE)
                  .applying(domainEvent);
            }

        )
    );

    when(WorkerState.ACTIVE,
        matchEvent(EventMessage.class,
            (event, data) -> {
              LOG.info("{} receive Message {}", getSelf(), event.getText());
              message.add(event.getText());
              MessageDomain domainEvent = new MessageDomain(event);
              return stay().applying(domainEvent);
            }

        )
    );

    when(WorkerState.ACTIVE,
        matchEvent(StoppedEvent.class,
            (event, data) -> {
              LOG.info("{} receive Stopped, size={}", getSelf(), data.getMessages().size());
              // 停止持久化的Actor前，需要删除持久化数据，否则重启后会自动恢复这个 Actor
              deleteMessages(lastSequenceNr());
              deleteSnapshot(snapshotSequenceNr());
              // 停止持久化的Actor前，需要发送 PoisonPill 给 Shard，否则这个 Actor 停止后将会被自动恢复
              getContext().getParent()
                  .tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
              return stop();
            }

        )
    );

    onTermination(
        matchStop(
            Normal(), (state, data) -> {
              LOG.info("{} termination, actorId={}, messageSize={}", getSelf(), data.getActorId(),
                  data.getMessages().size());
              if (data.getMessages().size() != 10) {
                LOG.error("***************** 不可用 Actor {}",data.getMessages().toArray(new String[0]));
              }
            }
        )
    );
  }

  @Override
  public void onRecoveryCompleted() {
    if (stateData().getMessages().size() != 10 && stateData().getMessages().size() != 0) {
      LOG.info("{} recovery completed state={}, actorId={}, messageSize={}", getSelf(), stateName(),
          stateData().getActorId(), stateData().getMessages().size());
    }
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
    LOG.debug("apply {} {}", getSelf(), domainEvent.getClass().getSimpleName());
    if (domainEvent instanceof StartDomain) {
      currentData.setActorId(domainEvent.getEvent().getActorId());
    } else if (domainEvent instanceof MessageDomain) {
      currentData.appendMessage(((MessageDomain) domainEvent).getEvent().getText());
    }
    return currentData;
  }
}
