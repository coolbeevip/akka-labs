package coolbeevip.labs.akka.java.cluster.actor;

import java.io.Serializable;

public interface StatsMessages {

  public static class Word implements  Serializable {
    private final String word;

    public Word(String word) {
      this.word = word;
    }

    public String getWord() {
      return word;
    }
  }
  public static class StatsJob implements Serializable {
    private final String text;

    public StatsJob(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  public static class StatsResult implements Serializable {
    private final double meanWordLength;

    public StatsResult(double meanWordLength) {
      this.meanWordLength = meanWordLength;
    }

    public double getMeanWordLength() {
      return meanWordLength;
    }

    @Override
    public String toString() {
      return "meanWordLength: " + meanWordLength;
    }
  }

  public static class JobFailed implements Serializable {
    private final String reason;

    public JobFailed(String reason) {
      this.reason = reason;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public String toString() {
      return "JobFailed(" + reason + ")";
    }
  }

}
