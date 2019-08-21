package coolbeevip.labs.akka.java.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.actor.ServiceActor;
import coolbeevip.labs.akka.java.cluster.actor.WorkerActor;

public class ComputeClusterApp {

  public static void main(String[] args) {
    // 创建三个节点，每个节点启动各启动一个 service、worker
    startup(new String[] { "2551", "2552", "0" });
  }

  public static void startup(String[] ports) {
    for (String port : ports) {
      Config config = 
        ConfigFactory.parseString(
          "akka.remote.netty.tcp.port=" + port)
          .withFallback(
              ConfigFactory.parseString("akka.cluster.roles = [compute]"))
          .withFallback(ConfigFactory.load("service"));

      ActorSystem system = ActorSystem.create("ClusterSystem", config);

      system.actorOf(Props.create(WorkerActor.class), "worker");
      system.actorOf(Props.create(ServiceActor.class), "service");
    }

  }
}
