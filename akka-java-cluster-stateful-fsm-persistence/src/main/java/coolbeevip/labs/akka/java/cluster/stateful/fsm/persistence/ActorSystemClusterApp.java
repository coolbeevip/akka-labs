package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StartedEvent;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.EventMessage;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.ShardRegionActor;
import coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence.actor.event.StoppedEvent;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActorSystemClusterApp {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public final static String TOPIC_NAME = "saga-event";
  public final static String BOOTSTRAP_SERVERS = "localhost:9092";

  public static void main(String[] args) {
    if (args.length == 0) {
      startup(new String[]{"2551", "2552", "0"});
    } else {
      startup(args);
    }
  }

  public static void startup(String[] ports) {
    for (String port : ports) {
      // 创建 Actor 系统
      final Config config =
          ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
              .withFallback(ConfigFactory.load());
      final ActorSystem system = ActorSystem.create("ClusterSystem", config);
      ActorRef shardRegionActor = system.actorOf(Props.create(ShardRegionActor.class));

      // 创建 Kafka Actor
      final Materializer materializer = ActorMaterializer.create(system);
      final Config consumerConfig = system.settings().config().getConfig("akka.kafka.consumer");
      // 创建 Topic
      initTopic(BOOTSTRAP_SERVERS, TOPIC_NAME);
      // 创建 consumer
      final ConsumerSettings<String, String> consumerSettings =
          ConsumerSettings
              .create(consumerConfig, new StringDeserializer(), new StringDeserializer())
              .withBootstrapServers(BOOTSTRAP_SERVERS)
              .withGroupId("group-saga")
              .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
              .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
              .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

      Consumer.atMostOnceSource(consumerSettings, Subscriptions.topics(TOPIC_NAME))
          .mapAsync(1, record -> {
            LOG.debug("key {}, value {}", record.key(), record.value());
            return CompletableFuture.completedFuture(record);
          })
          .to(Sink.foreach(record -> {
            if (record.value().equals("begin")) {
              shardRegionActor.tell(new StartedEvent(record.key()), shardRegionActor);
            } else if (record.value().equals("end")) {
              shardRegionActor.tell(new StoppedEvent(record.key()), shardRegionActor);
            } else {
              shardRegionActor.tell(new EventMessage(record.key(), record.value()), shardRegionActor);
            }
          }))
          .run(materializer);
    }
  }

  private static void initTopic(String bootstrapServers, String topicName) {
    final short replicationFactor = 1;
    final int partitions = 3;
    Map props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 50000);
    try (final AdminClient adminClient = KafkaAdminClient.create(props)) {
      try {
        final NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
        final CreateTopicsResult createTopicsResult = adminClient
            .createTopics(Collections.singleton(newTopic));
        createTopicsResult.values().get(topicName).get();
      } catch (InterruptedException | ExecutionException e) {
        if (!(e.getCause() instanceof TopicExistsException)) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    }
  }
}
