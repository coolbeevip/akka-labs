package coolbeevip.labs.akka.java.kafka.actor;

import java.io.Serializable;

public class Stop implements Serializable {
  final String actorId;

  public Stop(String actorId) {
    this.actorId = actorId;
  }
}
