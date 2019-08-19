package coolbeevip.labs.akka.java.fsm.event;

import java.util.LinkedList;
import java.util.List;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
public class RemoveItem implements Data {
  @Default
  private final List<String> items =  new LinkedList<>();

  public void remove(String item) {
    this.items.remove(item);
  }
}
