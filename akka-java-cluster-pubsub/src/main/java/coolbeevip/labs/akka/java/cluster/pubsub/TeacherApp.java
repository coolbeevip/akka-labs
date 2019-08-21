package coolbeevip.labs.akka.java.cluster.pubsub;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.StudentActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TeacherActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TopicMessages;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.Topics;
import java.util.concurrent.TimeUnit;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class TeacherApp {

  private static final Timeout lookupTimeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));
  public static void main(String[] args) throws Exception {
    Config config =
        ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
            .withFallback(ConfigFactory.load("pubsub"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config);
    ActorSelection actorSelection = system.actorSelection("akka.tcp://ClusterSystem@127.0.0.1:2551/user/Ms.Zhang");
    final Future<ActorRef> actorRefFuture = actorSelection.resolveOne(lookupTimeout);
    final ActorRef teacher = Await.result(actorRefFuture, lookupTimeout.duration());
    teacher.tell(new TopicMessages.Message(Topics.course_java, "python page 1-3"), ActorRef.noSender());
  }
}
