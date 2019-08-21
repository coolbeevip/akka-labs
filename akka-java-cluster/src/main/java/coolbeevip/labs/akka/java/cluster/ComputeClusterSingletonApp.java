package coolbeevip.labs.akka.java.cluster;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.actor.ServiceActor;

public class ComputeClusterSingletonApp {

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
          .withFallback(ConfigFactory.load("service-singleton"));

      ActorSystem system = ActorSystem.create("ClusterSystem", config);

      // 创建一个集群下的单例 ServiceActor
      ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system)
          .withRole("compute");
      system.actorOf(ClusterSingletonManager.props(
          Props.create(ServiceActor.class), PoisonPill.getInstance(), settings),
          "service");

      // 创建一个集群单例代理，代理 ServiceActor
      ClusterSingletonProxySettings proxySettings =
          ClusterSingletonProxySettings.create(system).withRole("compute");
      system.actorOf(ClusterSingletonProxy.props("/user/service",
          proxySettings), "serviceProxy");
    }

  }
}
