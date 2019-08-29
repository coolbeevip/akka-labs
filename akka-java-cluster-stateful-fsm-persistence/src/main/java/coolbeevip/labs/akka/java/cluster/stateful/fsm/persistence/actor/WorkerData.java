package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class WorkerData implements Serializable {
  private String actorId;
  @Default
  private List<String> messages = new ArrayList<>();

  public void appendMessage(String message){
    this.messages.add(message);
  }
}