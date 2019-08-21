package coolbeevip.labs.akka.java.cluster.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.ReachabilityEvent;
import akka.cluster.ClusterEvent.ReachableMember;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.JobFailed;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.StatsJob;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.StatsResult;
import coolbeevip.labs.akka.java.cluster.actor.StatsMessages.Word;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class ClientActor extends AbstractActor {

  final String servicePath;

  final LinkedBlockingQueue<Word> queue;

  final Set<Address> nodes = new HashSet<Address>();

  Cluster cluster = Cluster.get(getContext().system());

  public ClientActor(LinkedBlockingQueue<Word> queue, String servicePath) {
    this.queue = queue;
    this.servicePath = servicePath;
    QueueTake m1=new QueueTake(this.queue);
    Thread t1 =new Thread(m1);
    t1.start();
  }

  @Override
  public void preStart() {
    cluster.subscribe(self(), MemberEvent.class, ReachabilityEvent.class);
  }

  @Override
  public void postStop() {
    cluster.unsubscribe(self());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Word.class, t -> !nodes.isEmpty(), t -> {
          // just pick any one
          List<Address> nodesList = new ArrayList<>(nodes);
          Address address = nodesList.get(ThreadLocalRandom.current().nextInt(
              nodesList.size()));
          ActorSelection service = getContext().actorSelection(address + servicePath);
          System.out.println("发送到 "+service);
          service.tell(new StatsJob(t.getWord()),
              self());
        })
        .match(StatsResult.class, System.out::println)
        .match(JobFailed.class, System.out::println)
        .match(CurrentClusterState.class, state -> {
          nodes.clear();
          for (Member member : state.getMembers()) {
            if (member.hasRole("compute") && member.status().equals(MemberStatus.up())) {
              nodes.add(member.address());
            }
          }
        })
        .match(MemberUp.class, mUp -> {
          if (mUp.member().hasRole("compute")) {
            nodes.add(mUp.member().address());
          }
        })
        .match(MemberEvent.class, other -> {
          nodes.remove(other.member().address());
        })
        .match(UnreachableMember.class, unreachable -> {
          nodes.remove(unreachable.member().address());
        })
        .match(ReachableMember.class, reachable -> {
          if (reachable.member().hasRole("compute")) {
            nodes.add(reachable.member().address());
          }
        })
        .build();
  }

  class QueueTake implements Runnable {
    final LinkedBlockingQueue<Word> queue;

    public QueueTake(LinkedBlockingQueue<Word> queue) {
      this.queue = queue;
    }

    public void run() {
      while (true){
        if(!nodes.isEmpty()){
          try {
            getSelf().tell(queue.take(), ActorRef.noSender());
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }else{
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

}
