package coolbeevip.labs.akka.java.kafka.actor;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FsmData implements Serializable {
  private String globalTxId;

}