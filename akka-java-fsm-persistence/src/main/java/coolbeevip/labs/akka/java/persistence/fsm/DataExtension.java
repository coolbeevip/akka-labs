package coolbeevip.labs.akka.java.persistence.fsm;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import coolbeevip.labs.akka.java.persistence.fsm.DataExtension.DataExt;
import java.util.LinkedList;
import java.util.List;

public class DataExtension extends AbstractExtensionId<DataExt> {

  public static final DataExtension DATA_EXTENSION_PROVIDER = new DataExtension();

  @Override
  public DataExt createExtension(ExtendedActorSystem system) {
    return new DataExt();
  }

  public static class DataExt implements Extension {

    private final List<String> items = new LinkedList<>();

    public void addItem(String item) {
      items.add(item);
    }

    public List<String> getItems(){
      return items;
    }
  }
}
