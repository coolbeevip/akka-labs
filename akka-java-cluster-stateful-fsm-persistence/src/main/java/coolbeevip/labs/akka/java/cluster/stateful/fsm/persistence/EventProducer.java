package coolbeevip.labs.akka.java.cluster.stateful.fsm.persistence;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventProducer {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static void main(String[] args) {
    Properties props = new Properties();
    props.put("bootstrap.servers", ActorSystemClusterApp.BOOTSTRAP_SERVERS);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    long counter=0;
    for (int k = 0; k < 500; k++) {
      String globalTxId = counter+"-"+UUID.randomUUID().toString();
      producer.send(new ProducerRecord<String, String>(ActorSystemClusterApp.TOPIC_NAME,
          globalTxId, "begin"));
      for (int i = 0; i < 10; i++) {
        producer.send(new ProducerRecord<String, String>(ActorSystemClusterApp.TOPIC_NAME,
            globalTxId, String.valueOf(i)));
//        try {
//          Thread.sleep(100);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
        //producer.flush();
      }
      producer.send(new ProducerRecord<String, String>(ActorSystemClusterApp.TOPIC_NAME,
          globalTxId, "end"));
      counter++;
      LOG.info("{} {}",counter, globalTxId);

    }
    System.out.println("EventMessage sent successfully. total "+counter);
    producer.flush();
    producer.close();
  }
}
