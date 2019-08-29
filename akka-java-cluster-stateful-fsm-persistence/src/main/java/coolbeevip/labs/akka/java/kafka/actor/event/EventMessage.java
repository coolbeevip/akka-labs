package coolbeevip.labs.akka.java.kafka.actor.event;

import coolbeevip.labs.akka.java.kafka.actor.event.Event;

public class EventMessage extends Event {

  final String text;

  public EventMessage(String actorId, String text) {
    super(actorId);
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
