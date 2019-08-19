package coolbeevip.labs.akka.java.persistence.fsm.event;

import coolbeevip.labs.akka.java.persistence.fsm.domain.DomainEvent;
import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
public class AddItem implements Data {
  @Default
  private final List<String> items = new LinkedList<>();

  public void add(String item) {
    this.items.add(item);
  }
}
