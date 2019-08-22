package coolbeevip.labs.akka.java.cluster.sharding.actor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;

public class Devices extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private final ActorRef deviceRegion;

  private final Random random = new Random();

  private final Integer numberOfDevices = 50;

  // 自定义 ShardRegion 实体标示提取器
  static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {
    @Override
    public String entityId(Object message) {
      if (message instanceof Device.RecordTemperature)
        return String.valueOf(((Device.RecordTemperature) message).deviceId);
      else
        return null;
    }

    @Override
    public Object entityMessage(Object message) {
      if (message instanceof Device.RecordTemperature)
        return message;
      else
        return message;
    }

    @Override
    public String shardId(Object message) {
      int numberOfShards = 100; // 定义分片数 100
      if (message instanceof Device.RecordTemperature) {
        long id = ((Device.RecordTemperature) message).deviceId;
        return String.valueOf(id % numberOfShards); // 余数分片
        // Needed if you want to use 'remember entities':
        // } else if (message instanceof ShardRegion.StartEntity) {
        //   long id = ((ShardRegion.StartEntity) message).id;
        //   return String.valueOf(id % numberOfShards)
      } else {
        return null;
      }
    }
  };

  public enum UpdateDevice {
    INSTANCE
  }

  public Devices() {
    ActorSystem system = getContext().getSystem();

    // 创建共享集群配置，启动 ShardRegion 并在 Region 中注册 Actor
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);
    deviceRegion = ClusterSharding.get(system)
        .start(
            "Counter",
            Props.create(Device.class),
            settings,
            messageExtractor);

    // 加载延迟 10秒后每1秒给自己发送一个 UpdateDevice.INSTANCE 事件
    getContext().getSystem().scheduler().schedule(
        Duration.create(10, TimeUnit.SECONDS),
        Duration.create(1, TimeUnit.SECONDS),
        getSelf(),
        UpdateDevice.INSTANCE,
        system.dispatcher(),
        null
    );
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(UpdateDevice.class, u -> {
          // 生成50以内的随机数作为 deviceId, 并模拟生成一个温度消息发给 DeviceRegion
          Integer deviceId = random.nextInt(numberOfDevices);
          Double temperature = 5 + 30 * random.nextDouble();
          Device.RecordTemperature msg = new Device.RecordTemperature(deviceId, temperature);
          log.info("Sending {}", msg);
          deviceRegion.tell(msg, getSelf());
        })
        .build();
  }
}