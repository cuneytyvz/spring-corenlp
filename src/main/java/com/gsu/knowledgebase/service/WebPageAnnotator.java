package com.gsu.knowledgebase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gsu.common.util.MyHttpClient;
import com.gsu.knowledgebase.model.Entity;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Component
public class WebPageAnnotator {

    public static void main(String args[]) throws Exception {
        String html = IOUtils.toString(new URI("http://explodingart.com/wp/"));
        String text = ArticleExtractor.INSTANCE.getText(html);

        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);
    }

    public List<String> annotateWebPage(String url) throws Exception {
        String html = IOUtils.toString(new URI(url));
        String text = ArticleExtractor.INSTANCE.getText(html);
        String cut = text.length() < 5000 ? text : text.substring(0, 5000);

        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        cut +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);
//        if (response.get("Resources") == null) {
//            return null;
//        }

        List<String> entities = new ArrayList<>();
        for (Map r : (List<Map>) response.get("Resources")) {
            entities.add((String) r.get("@URI"));
        }

        return entities;
    }

    public String getWebPageTitle(String uri) throws Exception {
        Document doc = Jsoup.connect(uri).get();

        if (doc == null || doc.select("title") == null) {
            return null;
        }

        return doc.select("title").html();
    }

    public class Request {
        private String text;
        private double confidence = 0.5;
        private double support = 0;
        private String spotter = "Default";
        private String policy = "whitelist";
        private String sparql = "";

        public Request() {
        }

        public Request(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public double getSupport() {
            return support;
        }

        public void setSupport(double support) {
            this.support = support;
        }

        public String getSpotter() {
            return spotter;
        }

        public void setSpotter(String spotter) {
            this.spotter = spotter;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getSparql() {
            return sparql;
        }

        public void setSparql(String sparql) {
            this.sparql = sparql;
        }
    }
}

//Map response = new RestTemplate().getForObject(
//        "http://model.dbpedia-spotlight.org/en/annotate?text=" +
//                text +
//                "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
//        , Map.class);

//HttpHeaders headers = new HttpHeaders();
//headers.setAccept(Arrays.asList(MediaType.ALL));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Request> request = new HttpEntity<Request>(new Request(text), headers);
//
//        Object response = new RestTemplate().postForObject(
//        "http://spotlight.dbpedia.org/dev/rest/annotate/", request, Object.class);