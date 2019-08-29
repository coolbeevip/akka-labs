package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain;

import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.EventMessage;

public class MessageDomain implements Domain {

  private final EventMessage event;

  public MessageDomain(EventMessage event) {
    this.event = event;
  }

  @Override
  public EventMessage getEvent() {
    return this.event;
  }
}
