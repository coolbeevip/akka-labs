package coolbeevip.labs.akka.java.kafka.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import coolbeevip.labs.akka.java.kafka.actor.event.Event;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteSingletonActor extends AbstractActor {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ActorRef workerRegion;

  // 定义ShardRegion
  static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {
    @Override
    public String entityId(Object message) {
      if (message instanceof Event) {
        return ((Event) message).getActorId();
      } else {
        return null;
      }
    }

    @Override
    public Object entityMessage(Object message) {
      return message;
    }

    @Override
    public String shardId(Object message) {
      int numberOfShards = 100; // 定义分片数 100
      if (message instanceof Event) {
        String actorId = ((Event) message).getActorId();
        return String.valueOf(actorId.hashCode() % numberOfShards);
      } else if (message instanceof ShardRegion.StartEntity) {
        // Needed if you want to use 'remember entities'
        String actorId = ((ShardRegion.StartEntity) message).entityId();
        return String.valueOf(actorId.hashCode() % numberOfShards);
      } else {
        return null;
      }
    }
  };

  public RouteSingletonActor() {
    // 创建集群分片
    ActorSystem system = getContext().getSystem();
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    workerRegion = ClusterSharding.get(system)
        .start(
            "worker",
            Props.create(FsmActor.class),
            settings,
            messageExtractor);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .matchAny(msg -> {
          workerRegion.tell(msg, getSelf());
        })
        .build();
  }
}
