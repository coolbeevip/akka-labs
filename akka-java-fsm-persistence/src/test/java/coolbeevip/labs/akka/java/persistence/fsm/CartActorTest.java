package coolbeevip.labs.akka.java.persistence.fsm;

import static org.junit.Assert.assertEquals;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.persistence.fsm.event.AddItem;
import coolbeevip.labs.akka.java.persistence.fsm.event.Payment;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

public class CartActorTest extends JUnitSuite {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("CartActorTest", ConfigFactory.parseMap(getPersistenceMemConfig()));
  }

  @AfterClass
  public static void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  private String genPersistenceId() {
    return UUID.randomUUID().toString();
  }

  private static Map<String,Object> getPersistenceMemConfig(){
    Map<String, Object> map = new HashMap<>();
    map.put("akka.persistence.journal.plugin", "akka.persistence.journal.inmem");
    map.put("akka.persistence.journal.leveldb.dir", "target/example/journal");
    map.put("akka.persistence.snapshot-store.plugin", "akka.persistence.snapshot-store.local");
    map.put("akka.persistence.snapshot-store.local.dir", "target/example/snapshots");
    return map;
  }

  private static Map<String,Object> getPersistenceRedisConfig(){
    Map<String, Object> map = new HashMap<>();
    map.put("akka.actor.warn-about-java-serializer-usage",false);
    map.put("akka.persistence.journal.plugin", "akka-persistence-redis.journal");
    map.put("akka.persistence.snapshot-store.plugin", "akka-persistence-redis.snapshot");
    map.put("akka-persistence-redis.redis.mode", "simple");
    map.put("akka-persistence-redis.redis.host", "localhost");
    map.put("akka-persistence-redis.redis.port", "6379");
    map.put("akka-persistence-redis.redis.database", "0");
    return map;
  }

  @Test
  public void test() {
    new TestKit(system) {
      {
        final ActorRef cartActor = system.actorOf(CartActor.props(genPersistenceId()));
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

