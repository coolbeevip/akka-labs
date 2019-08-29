package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event;


public class StartedEvent extends Event {

  public StartedEvent(String actorId) {
    super(actorId);
  }
}
