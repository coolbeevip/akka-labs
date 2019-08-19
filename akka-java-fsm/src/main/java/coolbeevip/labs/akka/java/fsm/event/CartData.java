package coolbeevip.labs.akka.java.fsm.event;

import java.util.LinkedList;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public class CartData implements Data {
  @Default
  private final List<String> item =  new LinkedList<>();

}
