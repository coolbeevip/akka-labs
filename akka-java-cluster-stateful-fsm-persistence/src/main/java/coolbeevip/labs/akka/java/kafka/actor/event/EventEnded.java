package coolbeevip.labs.akka.java.kafka.actor.event;

import coolbeevip.labs.akka.java.kafka.actor.event.Event;

public class EventEnded extends Event {

  public EventEnded(String actorId) {
    super(actorId);
  }
}
