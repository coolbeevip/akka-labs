package coolbeevip.labs.akka.java.fsm;

import static org.junit.Assert.assertEquals;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import coolbeevip.labs.akka.java.fsm.event.AddItem;
import coolbeevip.labs.akka.java.fsm.event.Payment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

public class CartActorTest extends JUnitSuite {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("CartActorTest");
  }

  @AfterClass
  public static void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void test() {
    new TestKit(system) {
      {
        final ActorRef cartActor = system.actorOf(Props.create(CartActor.class));
        AddItem addItem1 = AddItem.builder().build();
        addItem1.add("book");
        cartActor.tell(addItem1, ActorRef.noSender());
        AddItem addItem2 = AddItem.builder().build();
        addItem2.add("pen");
        addItem2.add("pen");
        cartActor.tell(addItem2, ActorRef.noSender());
        cartActor.tell(Payment.builder().build(), ActorRef.noSender());
        expectNoMessage();
        assertEquals(3,DataExtension.DATA_EXTENSION_PROVIDER.get(system).getItems().size());
        system.stop(cartActor);
      }
    };
  }
}

