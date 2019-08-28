package coolbeevip.labs.akka.java.kafka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import coolbeevip.labs.akka.java.kafka.actor.Message;
import coolbeevip.labs.akka.java.kafka.actor.RouteSingletonActor;
import coolbeevip.labs.akka.java.kafka.actor.Stop;
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

public class ConsumerApp {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public final static String TOPIC_NAME = "saga-event";
  public final static String BOOTSTRAP_SERVERS = "localhost:9092";

  public static void main(String[] args) {
    if(args.length==0){
      startup(new String[]{"2551", "2552", "0"});
    }else{
      startup(args);
    }
  }

  public static void startup(String[] ports) {
    for (String port : ports) {
      // init ActorSystem
      final Config config =
          ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
              .withFallback(ConfigFactory.parseString("akka.cluster.roles = [compute]"))
              .withFallback(ConfigFactory.load("route-singleton"));
      final ActorSystem system = ActorSystem.create("ClusterSystem", config);

      // 创建单例RouteActor
      ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system).withRole("compute");
      system.actorOf(ClusterSingletonManager.props(
          Props.create(RouteSingletonActor.class), PoisonPill.getInstance(), settings),
          "routeActor");
      ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system).withRole("compute");
      ActorRef routeActorProxy = system.actorOf(ClusterSingletonProxy.props("/user/routeActor",
          proxySettings), "routeActorProxy");

      final Materializer materializer = ActorMaterializer.create(system);

      // load kafka config
      final Config consumerConfig = system.settings().config().getConfig("akka.kafka.consumer");

      // initTopic
      initTopic(BOOTSTRAP_SERVERS, TOPIC_NAME);

      // init consumer
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
            if(record.value().equals("stop")){
              routeActorProxy.tell(new Stop(record.key()),routeActorProxy);
            }else{
              routeActorProxy.tell(new Message(record.key(),record.value()),routeActorProxy);
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
