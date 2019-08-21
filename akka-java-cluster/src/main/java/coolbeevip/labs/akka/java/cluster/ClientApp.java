package coolbeevip.labs.akka.java.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.actor.ClientActor;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.Word;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientApp {

  public static void main(String[] args) {
    final Queue<Word> queue = new LinkedBlockingQueue<>();
    // 创建一个 Actor 加入这个集群，并发送计算字符串平均长度
    ActorSystem system = ActorSystem.create("ClusterSystem",
        ConfigFactory.load("service"));
    ActorRef acotr = system.actorOf(Props.create(ClientActor.class, queue, "/user/service"),
        "client");
    queue.add(new Word("hello tom"));
    queue.add(new Word("hello kat"));
    queue.add(new Word("who is she"));
    queue.add(new Word("she is my sister"));
  }
}
