/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solrloadtester;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author kevin
 */
public class SolrLoadTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Log.init();
             System.out.println(" *************** BEGIN TEST RUN ************* ");
            Properties props = new Properties();
            props.load(new FileReader(new File("tester.properties")));
            long throttle = Long.parseLong(props.getProperty("throttle_by"));
            int maxruns = Integer.parseInt(props.getProperty("maxruns"));
            HttpClient client = HttpClientBuilder.create()
                    .build();

            String query = props.getProperty("url");//"http://localhost:8983/solr/hud/sql?stmt=SELECT%20id%20FROM%20hud%20LIMIT%2010";
            HttpResponse response = null;
            final HttpGet get = new HttpGet(query);
            final StopWatch watch = new StopWatch();

            String result = "";
            for (int i = 0; i < maxruns; i++) {
                if(throttle > 0){
                    Thread.sleep(throttle);
                }
                watch.start();
                response = client.execute(get);
                watch.stop();
                long time = watch.getElapsedTime();
                if (response != null) {
                    result = Utils.streamToString(response.getEntity().getContent());
                    Log.log(getLogResponseLine(result, time, response.getStatusLine().getStatusCode()));
                    System.out.println("Response: "+time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
          System.out.println(" *************** END TEST RUN ************* ");
    }
    
    
    private static String getLogResponseLine(String content, long time, int status){
        String result = "";
        result += "[" + new Date().toString() + "]( Request_to_Response_Time="+time+"){ status: "+status+" } " +content + "\n";
        return result;
    }

}
