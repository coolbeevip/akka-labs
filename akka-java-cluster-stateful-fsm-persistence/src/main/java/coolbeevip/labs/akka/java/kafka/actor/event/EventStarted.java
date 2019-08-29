package coolbeevip.labs.akka.java.kafka.actor.event;


import coolbeevip.labs.akka.java.kafka.actor.event.Event;

public class EventStarted extends Event {

  public EventStarted(String actorId) {
    super(actorId);
  }
}
