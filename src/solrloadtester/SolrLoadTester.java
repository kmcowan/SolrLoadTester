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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

/**
 *
 * @author kevin
 */
public class SolrLoadTester {

    private static final HttpClient client = HttpClientBuilder.create()
            .build();
    private static CloudSolrClient solrClient = null;
    private static final ThreadQueue queue = ThreadQueue.getInstance();
    private static final StatsTracker stats = StatsTracker.getInstance();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // final Timer timer = new Timer();

        try {
            Log.init();
            System.out.println(" *************** BEGIN TEST RUN ************* ");
            Properties props = new Properties();
            props.load(new FileReader(new File("tester.properties")));
            long throttle = Long.parseLong(props.getProperty("throttle_by"));
            int maxruns = Integer.parseInt(props.getProperty("maxruns"));
            int coreThreads = Integer.parseInt(props.getProperty("core.threads"));
            int maxThreads = Integer.parseInt(props.getProperty("max.threads"));

            //ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(coreThreads);
            final ScheduledExecutorService executor = Executors.newScheduledThreadPool(maxThreads);
            //  executor.setMaximumPoolSize(maxThreads);
            int i = 0;

            Callable<Double> callableTask = () -> {
                System.out.println("Callable called...");
                return 0.0d;

            };

            executor.submit(callableTask);

            String query = props.getProperty("url");//"http://localhost:8983/solr/hud/sql?stmt=SELECT%20id%20FROM%20hud%20LIMIT%2010";
            HttpResponse response = null;
            final HttpGet get = new HttpGet(query);
            final StopWatch watch = new StopWatch();
            boolean useThreading = Boolean.parseBoolean(props.getProperty("multithreading"));
            boolean useThrottling = false;

            if (throttle > 0) {
                useThrottling = true;
                System.out.println("Throttling Enabled... " + throttle + " ms wait...");
            }

            if (useThreading) {
                System.out.println("Threading Enabled... ");
            }

            boolean useSolrClient = Boolean.parseBoolean(props.getProperty("usezookeeper"));

            if (useSolrClient) {
                System.out.println("Solr Client enabled...");
            }
            String handler = props.getProperty("solr.handler");
            String solrQuery = props.getProperty("solr.query");
            if (useSolrClient) {
                String collection = props.getProperty("solr.collection");

                String server = props.getProperty("zk.url");
                solrClient = getSolrClient(server, collection);
                if (solrClient == null) {
                    useSolrClient = false;
                    Log.log("ERROR CREATING SOLR CLIENT... FALLBACK TO HTTPCLIENT INSTEAD. ");
                }
            }

            String result = "";
            SolrQuery squery = null;
            for (i = 0; i < maxruns; i++) {
                if (useSolrClient && !useThreading) {
                    squery = new SolrQuery(query);
                    squery.setRequestHandler(handler);
                    squery.setQuery(solrQuery);

                    QueryRunner runner = new QueryRunner(squery);
                    //  timer.schedule(runner, 50);
                    String id = UUID.randomUUID().toString();
                    runner.setId(id);
                    queue.put(id, runner);

                    executor.schedule(runner, throttle, TimeUnit.MILLISECONDS);

                } else if (useSolrClient && useThreading) {
                    squery = new SolrQuery(query);
                    squery.setRequestHandler(handler);
                    squery.setQuery(solrQuery);
                    squery.setParam("wt", "json");
                    squery.set("wt", "json");
                    QueryRunner runner = new QueryRunner(squery);
                    String id = UUID.randomUUID().toString();
                    runner.setId(id);
                    queue.put(id, runner);

                    // timer.schedule(runner, throttle);
                    executor.schedule(runner, throttle, TimeUnit.MILLISECONDS);
                } else if (useThrottling && !useThreading) {
                    Thread.sleep(throttle);
                    watch.start();
                    response = client.execute(get);
                    watch.stop();
                    long time = watch.getElapsedTime();
                    if (response != null) {
                        result = Utils.streamToString(response.getEntity().getContent());
                        Log.log(getLogResponseLine(result, time, response.getStatusLine().getStatusCode()));
                        System.out.println("Response: " + time);
                        stats.addResponseTime(time);
                    }
                } else if (useThrottling && useThreading) {
                    QueryRunner runner = new QueryRunner(query);
                    String id = UUID.randomUUID().toString();
                    runner.setId(id);
                    queue.put(id, runner);
                    //  timer.schedule(runner, throttle);
                    executor.schedule(runner, throttle, TimeUnit.MILLISECONDS);
                } else if (!useThrottling && useThreading) {
                    QueryRunner runner = new QueryRunner(query);
                    String id = UUID.randomUUID().toString();
                    runner.setId(id);
                    queue.put(id, runner);
                    //  timer.schedule(runner, 50);
                    executor.schedule(runner, throttle, TimeUnit.MILLISECONDS);

                } else { // default 
                    watch.start();
                    response = client.execute(get);
                    watch.stop();
                    long time = watch.getElapsedTime();
                    if (response != null) {
                        result = Utils.streamToString(response.getEntity().getContent());
                        Log.log(getLogResponseLine(result, time, response.getStatusLine().getStatusCode()));
                        System.out.println("Response: " + time);
                        stats.addResponseTime(time);

                    }
                }
            }
            if (useThreading) {
                long counts = 0;
                while (queue.size() > 0) {
                    if (counts == 100000000) {
                        System.out.println("wait for queue to clear..." + queue.size());
                        counts = 0;
                    }

                    counts++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" \n  ");
        System.out.println(" --------------------------------------- ");
        System.out.println(" Stats:  ");
        System.out.println(" Number of queries:  " + stats.getResponseTimes().size());
        System.out.println(" Average Response Time:  " + stats.getAverageResponseTime());
        System.out.println(" Longest Query Time:  " + stats.getMaxTime());
        System.out.println(" Shortest Query Time:  " + stats.getMinTime());
        System.out.println(" --------------------------------------- ");
        System.out.println(" \n  ");
        System.out.println(" *************** END TEST RUN ************* ");
        System.exit(0);
    }

    protected static String getLogResponseLine(String content, long time, int status) {
        String result = "";
        result += "[" + new Date().toString() + "]( Request_to_Response_Time=" + time + "){ status: " + status + " } " + content + "\n";
        return result;
    }

    protected static CloudSolrClient getSolrClient(String server, String collection) {
        CloudSolrClient cloudServer = null;

        try {
            PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
            //  HttpClient client = new DefaultHttpClient(cm);
            cloudServer = new CloudSolrClient(server, client);
            cloudServer.setDefaultCollection(collection);
            Log.log("CLOUD SERVER INIT OK...");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cloudServer;
    }

    /**
     * @return the client
     */
    public static HttpClient getClient() {
        return client;
    }

    public static SolrQuery getQuery() {
        SolrQuery query = new SolrQuery();

        return query;
    }

    /**
     * @return the solrClient
     */
    public static CloudSolrClient getSolrClient() {
        return solrClient;
    }

    /**
     * @return the queue
     */
    public static ThreadQueue getQueue() {
        return queue;
    }

    /**
     * @return the stats
     */
    public static StatsTracker getStats() {
        return stats;
    }

}
