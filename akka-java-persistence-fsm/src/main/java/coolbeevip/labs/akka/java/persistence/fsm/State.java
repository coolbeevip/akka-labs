package coolbeevip.labs.akka.java.persistence.fsm;

import akka.persistence.fsm.PersistentFSM;

public enum State implements PersistentFSM.FSMState {
  Empty,Shopping;

  @Override
  public String identifier() {
    return name();
  }
}
