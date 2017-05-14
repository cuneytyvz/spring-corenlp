package com.gsu.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * https://www.mkyong.com/java/apache-httpclient-examples/
 * https://www.mkyong.com/webservices/jax-rs/restful-java-client-with-apache-httpclient/
 */
public class MyHttpClient {

    private final String USER_AGENT = "Mozilla/5.0";

    public Map<String, Object> get(String url) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        Map<String, Object> map = new ObjectMapper().readValue(result.toString(),
                new TypeReference<Map<String, String>>() {
                });

        return map;
    }
}
