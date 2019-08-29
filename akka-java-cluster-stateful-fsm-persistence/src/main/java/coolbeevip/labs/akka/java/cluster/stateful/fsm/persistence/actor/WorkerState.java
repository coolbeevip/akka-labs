package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import akka.persistence.fsm.PersistentFSM;

public enum WorkerState implements PersistentFSM.FSMState {
  IDLE,
  ACTIVE;

  @Override
  public String identifier() {
    return name();
  }
}
