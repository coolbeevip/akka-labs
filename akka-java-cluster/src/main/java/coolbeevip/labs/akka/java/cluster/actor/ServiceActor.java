package coolbeevip.labs.akka.java.cluster.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.StatsJob;

public class ServiceActor extends AbstractActor {

  // This router is used both with lookup and deploy of routees. If you
  // have a router with only lookup of routees you can use Props.empty()
  // instead of Props.create(WorkerActor.class).
  ActorRef workerRouter = getContext().actorOf(
      FromConfig.getInstance().props(Props.create(WorkerActor.class)),
      "workerRouter");

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(StatsJob.class, job -> !job.getText().isEmpty(), job -> {
        System.out.println("收到消息:"+getSelf());
        String[] words = job.getText().split(" ");
        ActorRef replyTo = sender();

        // create actor that collects replies from workers
        ActorRef aggregator = getContext().actorOf(
          Props.create(AggregatorActor.class, words.length, replyTo));

        // send each word to a worker
        for (String word : words) {
          workerRouter.tell(new ConsistentHashableEnvelope(word, word),
            aggregator);
        }
      })
      .build();
  }
}
