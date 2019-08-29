package coolbeevip.labs.akka.java.kafka;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerApp {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put("bootstrap.servers", ConsumerApp.BOOTSTRAP_SERVERS);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = new KafkaProducer<>(props);
    for (int k = 0; k < 1; k++) {
      String globalTxId = UUID.randomUUID().toString();
      LOG.info("id {}", globalTxId);
      producer.send(new ProducerRecord<String, String>(ConsumerApp.TOPIC_NAME,
          globalTxId, "begin"));
      for (int i = 0; i < 10; i++) {
        producer.send(new ProducerRecord<String, String>(ConsumerApp.TOPIC_NAME,
            globalTxId, String.valueOf(i)));
      }
      producer.send(new ProducerRecord<String, String>(ConsumerApp.TOPIC_NAME,
          globalTxId, "end"));
    }
    producer.flush();
    producer.close();
  }
}
