package coolbeevip.labs.akka.java.cluster.pubsub.actor;

import java.io.Serializable;

public interface TopicMessages {

  public static class Message implements  Serializable {
    private final Topics course;
    private final String task;

    public Message(Topics course, String task) {
      this.course = course;
      this.task = task;
    }

    public String getTask() {
      return task;
    }

    public Topics getCourse() {
      return course;
    }
  }

}
