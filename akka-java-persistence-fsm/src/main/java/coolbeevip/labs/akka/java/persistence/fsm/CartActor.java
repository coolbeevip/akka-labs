package coolbeevip.labs.akka.java.persistence.fsm;

import static coolbeevip.labs.akka.java.persistence.fsm.State.Empty;
import static coolbeevip.labs.akka.java.persistence.fsm.State.Shopping;

import akka.actor.Props;
import akka.persistence.fsm.AbstractPersistentFSM;
import coolbeevip.labs.akka.java.persistence.fsm.domain.DomainEvent;
import coolbeevip.labs.akka.java.persistence.fsm.event.AddItem;
import coolbeevip.labs.akka.java.persistence.fsm.event.CartData;
import coolbeevip.labs.akka.java.persistence.fsm.event.Payment;
import coolbeevip.labs.akka.java.persistence.fsm.event.RemoveItem;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CartActor extends AbstractPersistentFSM<State, CartData, DomainEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final String persistenceId;

  public static Props props(String persistenceId) {
    return Props.create(CartActor.class, persistenceId);
  }

  public CartActor(String persistenceId) {
    this.persistenceId = persistenceId;
    {
      startWith(Empty, CartData.builder().build());

      when(Empty,
          matchEvent(
              AddItem.class,
              CartData.class,
              (event, data) -> {
                event.getItems().forEach(item -> data.getItem().add(item));
                return goTo(Shopping);
              }));

      when(Shopping,
          matchEvent(
              AddItem.class,
              CartData.class,
              (event, data) -> {
                event.getItems().forEach(item -> data.getItem().add(item));
                return stay();
              }));

      when(Shopping,
          matchEvent(
              RemoveItem.class,
              CartData.class,
              (event, data) -> {
                event.getItems().forEach(item -> data.getItem().remove(item));
                if (data.getItem().size() == 0) {
                  return goTo(Empty);
                } else {
                  return stay();
                }
              }));

      when(Shopping,
          matchEvent(
              Payment.class,
              CartData.class,
              (event, data) -> {
                return stop();
              }));

      onTermination(
          matchStop(
              Normal(), (state, data) -> {
                data.getItem().forEach(
                    item -> DataExtension.DATA_EXTENSION_PROVIDER.get(context().system())
                        .addItem(item));
                LOG.debug("You bought {} items", data.getItem().size());
              }
          )
      );
    }
  }

  @Override
  public Class domainEventClass() {
    return DomainEvent.class;
  }

  @Override
  public CartData applyEvent(DomainEvent domainEvent, CartData currentData) {
    return currentData;
  }

  @Override
  public String persistenceId() {
    return persistenceId;
  }
}
