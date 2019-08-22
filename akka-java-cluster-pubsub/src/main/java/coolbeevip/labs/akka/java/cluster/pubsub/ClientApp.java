package coolbeevip.labs.akka.java.cluster.pubsub;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClient;
import akka.cluster.client.ClusterClientSettings;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.StudentActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TeacherActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TopicMessages;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.Topics;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ClientApp {

  private static final Timeout lookupTimeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));
  public static void main(String[] args) throws Exception {
    Config config =
        ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
            .withFallback(ConfigFactory.load("pubsub"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config);

    final ActorRef c =
        system.actorOf(
            ClusterClient.props(
                ClusterClientSettings.create(system).withInitialContacts(initialContacts())),
            "client");

    c.tell(new ClusterClient.Send("/user/Mr.Zhang", new TopicMessages.Message(Topics.course_java, "python page 1-3"), true), ActorRef.noSender());
  }

  static Set<ActorPath> initialContacts() {
    return new HashSet<ActorPath>(
        Arrays.asList(
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"),
            ActorPaths.fromString("akka.tcp://ClusterSystem@127.0.0.1:2552/system/receptionist")));
  }
}
