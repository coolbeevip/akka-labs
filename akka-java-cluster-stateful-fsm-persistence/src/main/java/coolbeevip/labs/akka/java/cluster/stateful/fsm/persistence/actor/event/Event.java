package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event;

import java.io.Serializable;

public abstract class Event implements Serializable {
  final String actorId;

  public Event(String actorId) {
    this.actorId = actorId;
  }

  public String getActorId() {
    return actorId;
  }
}
