/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solrloadtester;

import java.util.TimerTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author kevin
 */
public class QueryRunner extends TimerTask implements Runnable {
    
    private String url = null;
    final   HttpClient client = HttpClientBuilder.create()
                    .build();
    public QueryRunner(String url){
        this.url = url;
    }
    @Override
    public void run(){
             final StopWatch watch = new StopWatch();
             HttpGet get = null;
             HttpResponse response = null;
             String result = "";
             
        try{
             watch.start();
                    response = client.execute(get);
                    watch.stop();
                    long time = watch.getElapsedTime();
                    if (response != null) {
                        result = Utils.streamToString(response.getEntity().getContent());
                        Log.log(SolrLoadTester.getLogResponseLine(result, time, response.getStatusLine().getStatusCode()));
                        System.out.println("Response: " + time);
                    }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
