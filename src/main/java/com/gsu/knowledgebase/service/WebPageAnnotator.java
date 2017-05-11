package com.gsu.knowledgebase.service;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by cnytync on 11/05/2017.
 */
public class WebPageAnnotator {

    public static void main(String args[]) throws Exception {
        String html = IOUtils.toString(new URI("http://explodingart.com/wp/"));
        String text = ArticleExtractor.INSTANCE.getText(html);

        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);
        System.out.println();
    }

    public List<Map<String, String>> annotateWebPage(String url) throws Exception {
        String html = IOUtils.toString(new URI("https://www.quora.com/What-is-Bayesian-Program-Learning-BPL"));
        String text = ArticleExtractor.INSTANCE.getText(html);

        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);

        if (response.get("Resources") != null) {
            return (List<Map<String, String>>) response.get("Resources");
        } else {
            return null;
        }
    }
}