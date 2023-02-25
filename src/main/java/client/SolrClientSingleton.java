package client;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

public class SolrClientSingleton {
    private static SolrClient solrClient;

    private SolrClientSingleton() {
        // private constructor to prevent instantiation from outside
    }

    public static synchronized SolrClient getSolrClient() {
        if (solrClient == null) {
            String solrUrl = "http://localhost:8983/solr/productCatalog";
            solrClient = new HttpSolrClient.Builder(solrUrl).build();
        }
        return solrClient;
    }


}
