package coolbeevip.labs.akka.java.kafka.actor;

import java.io.Serializable;

public class Message implements Serializable {
  final String actorId;
  final String text;

  public Message(String actorId, String text) {
    this.actorId = actorId;
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public String getActorId() {
    return actorId;
  }
}
