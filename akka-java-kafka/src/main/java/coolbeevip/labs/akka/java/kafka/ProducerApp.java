package coolbeevip.labs.akka.java.kafka;

import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class ProducerApp {

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
    long counter=0;
    for (int k = 0; k < 1; k++) {
      String globalTxId = UUID.randomUUID().toString();
      for (int i = 0; i < 10; i++) {
        producer.send(new ProducerRecord<String, String>(ConsumerApp.TOPIC_NAME,
            globalTxId, String.valueOf(i)));
      }
      producer.send(new ProducerRecord<String, String>(ConsumerApp.TOPIC_NAME,
          globalTxId, "stop"));
    }
    System.out.println("Message sent successfully. total "+counter);
    producer.flush();
    producer.close();
  }
}
