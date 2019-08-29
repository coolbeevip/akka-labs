package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.domain;

import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.Event;
import java.io.Serializable;

public interface Domain extends Serializable {
  Event getEvent();
}
