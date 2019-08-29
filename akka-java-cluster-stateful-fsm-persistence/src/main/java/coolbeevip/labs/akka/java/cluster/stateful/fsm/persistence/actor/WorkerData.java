package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WorkerData implements Serializable {
  private String globalTxId;

}