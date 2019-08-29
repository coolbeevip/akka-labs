package coolbeevip.labs.akka.java.kafka.actor;

import akka.persistence.fsm.PersistentFSM;

public enum FsmState implements PersistentFSM.FSMState {
  IDLE,
  ACTIVE;

  @Override
  public String identifier() {
    return name();
  }
}
