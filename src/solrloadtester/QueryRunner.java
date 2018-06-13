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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.json.JSONObject;

/**
 *
 * @author kevin
 */
public class QueryRunner extends TimerTask implements Runnable {

    private String url = null;
    private SolrQuery query = null;
    private boolean usingSolrClient = false;
    private String id = "";

    final HttpClient client = SolrLoadTester.getClient();
    private CloudSolrClient solrClient = null;
    public QueryRunner(String url) {
        this.url = url;
       
    }

    public QueryRunner(SolrQuery query) {
        this.query = query;
        this.solrClient = SolrLoadTester.getSolrClient();
    }

    @Override
    public void run() {
        final StopWatch watch = new StopWatch();
        HttpGet get = null;
        HttpResponse response = null;
        String result = "";

        try {
             watch.start();
            if (query != null) {
                QueryResponse sresponse = solrClient.query(query);
                watch.stop();
                 long time = watch.getElapsedTime();
                if (sresponse != null) {
                
                    result = sresponse.toString();
                    int status = 200;
                  //  System.out.println(result);
                  //  System.out.println("Status: "+sresponse.getHeader().get("status"));
                   
                     if(sresponse.getHeader().get("status").equals("0")){
                         status = Integer.parseInt((String)sresponse.getHeader().get("status"));
                     }
                     Log.log(SolrLoadTester.getLogResponseLine(result, time, status));
                    System.out.println("Response: " + time);
                     SolrLoadTester.getStats().addResponseTime(time);
                }
            } else {
               
                response = client.execute(get);
                watch.stop();
                long time = watch.getElapsedTime();
                if (response != null) {
                    result = Utils.streamToString(response.getEntity().getContent());
                    Log.log(SolrLoadTester.getLogResponseLine(result, time, response.getStatusLine().getStatusCode()));
                    System.out.println("Response: " + time);
                     SolrLoadTester.getStats().addResponseTime(time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SolrLoadTester.getQueue().remove(this.id);
        }
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the query
     */
    public SolrQuery getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(SolrQuery query) {
        this.query = query;
    }

    /**
     * @return the usingSolrClient
     */
    public boolean isUsingSolrClient() {
        return usingSolrClient;
    }

    /**
     * @param usingSolrClient the usingSolrClient to set
     */
    public void setUsingSolrClient(boolean usingSolrClient) {
        this.usingSolrClient = usingSolrClient;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
