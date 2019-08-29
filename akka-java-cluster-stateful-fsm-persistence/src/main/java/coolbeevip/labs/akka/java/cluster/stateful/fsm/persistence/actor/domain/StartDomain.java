package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain;

import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.Event;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StartedEvent;

public class StartDomain implements Domain {

  private final StartedEvent event;

  public StartDomain(StartedEvent event) {
    this.event = event;
  }

  @Override
  public Event getEvent() {
    return this.event;
  }
}
