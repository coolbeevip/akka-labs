package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain;

import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.Event;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StoppedEvent;

public class StopDomain implements Domain {

  private final StoppedEvent event;

  public StopDomain(StoppedEvent event) {
    this.event = event;
  }

  @Override
  public Event getEvent() {
    return this.event;
  }
}
