package coolbeevip.labs.akka.java.cluster.pubsub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.client.ClusterClientReceptionist;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.StudentActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TeacherActor;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.TopicMessages;
import coolbeevip.labs.akka.java.cluster.pubsub.actor.Topics;

public class SchoolApp {

  public static void main(String[] args) {
    String[] ports = new String[]{"2551", "2552", "0"};
    ActorRef teacher = null;
    for (String port : ports) {
      Config config =
          ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
              .withFallback(ConfigFactory.load("pubsub"));

      ActorSystem system = ActorSystem.create("ClusterSystem", config);

      system.actorOf(Props.create(StudentActor.class, Topics.course_java.name()),
          "Student." + port);

      if (teacher == null) {
        teacher = system.actorOf(Props.create(TeacherActor.class), "Mr.Zhang");
        ClusterClientReceptionist.get(system).registerService(teacher);
      }
    }
  }
}
