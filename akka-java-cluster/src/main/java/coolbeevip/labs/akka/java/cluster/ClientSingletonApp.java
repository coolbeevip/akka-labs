package coolbeevip.labs.akka.java.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.actor.ClientActor;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.Word;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSingletonApp {

  public static void main(String[] args) {
    final Queue<Word> queue = new LinkedBlockingQueue<>();
    ActorSystem system = ActorSystem.create("ClusterSystem",
        ConfigFactory.load("service-singleton"));
    system.actorOf(Props.create(ClientActor.class, queue, "/user/serviceProxy"),
        "client");
    queue.add(new Word("1"));
    queue.add(new Word("12"));
    queue.add(new Word("1234"));
  }
}
