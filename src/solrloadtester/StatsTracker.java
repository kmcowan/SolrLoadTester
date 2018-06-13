/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solrloadtester;

import java.util.ArrayList;

/**
 *
 * @author kevin
 */
public class StatsTracker {
    
    private  ArrayList<Long> responseTimes = null;
    private long maxTime = 0;
    private long minTime = 1000000000;
    
    private StatsTracker(){
        
    }
    
    public static StatsTracker getInstance(){
        return new StatsTracker();
    }
    public  void addResponseTime(long time){
        if(responseTimes == null){
            responseTimes = new ArrayList<>();
        }
        if(time > maxTime){
            maxTime = time;
        }
        
        if(time < minTime){
            minTime = time;
        }
        responseTimes.add(time);
    }
    
    public double getAverageResponseTime(){
        double time = 0.0d;
        long avg = 0;
        for(int i=0; i<responseTimes.size(); i++){
            avg = avg + responseTimes.get(i);
        }
        return avg / responseTimes.size();
    }

    /**
     * @return the responseTimes
     */
    public ArrayList<Long> getResponseTimes() {
        return responseTimes;
    }

    /**
     * @return the maxTime
     */
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * @return the minTime
     */
    public long getMinTime() {
        return minTime;
    }
}
