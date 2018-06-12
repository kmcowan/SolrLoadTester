package solrloadtester;

import java.util.Date;

public class StopWatch {
  private long startTime;
  private long stopTime;
  private boolean running;
  private Date startDate = null;

  /**
  * Starts the watch.
  */
  public void start() {
    if (!running) {
      running = true;
      startTime = System.currentTimeMillis();
      startDate = new Date();
    }
  }

  /**
  * Stops the watch.
  */
  public void stop() {
    if (running) {
      running = false;
      stopTime = System.currentTimeMillis();
    }
  }

  /**
  * Returns the elapsed time. If the watch has been stopped,
  * this returns the amount of time between when the watch was
  * started and stopped. If the watch has not been stopped, this
  * returns the amount of time between when the watch was started
  * and this method was invoked.
  * @return The elapsed time in milliseconds.
  */
  public long getElapsedTime() {
    if (!running)
      return stopTime - startTime;
    else
      return System.currentTimeMillis() - startTime;
  }

  public String getStartDate(){
      String result = "not started";
      if(startDate != null){
          result = startDate.toString(); 
      }
      return result;
  }

  public String getFriendlyElapsedTime(){
      String result = "";
    
      double seconds = (getElapsedTime() / 1000); // convert to seconds
      result = Math.round(seconds / 60) + " Minutes"; // convert to minutes
      return result;
  }

  public static String getConvertedTime(long t){
      String result = "";
       double seconds = (t / 1000); // convert to seconds
      result = Math.round(seconds / 60) + " Minutes"; // convert to minutes
      return result;
  }

  /**
  * Resets the watch.
  */
  public void reset() {
    running = false;
    startTime = 0;
    stopTime = 0;
  }


}


