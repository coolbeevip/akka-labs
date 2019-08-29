package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event;

public class StoppedEvent extends Event {

  public StoppedEvent(String actorId) {
    super(actorId);
  }
}
