package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event;

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
