package com.gsu.knowledgebase.service;

import com.gsu.knowledgebase.model.Entity;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        System.out.println();
    }

    public List<String> annotateWebPage(String url) throws Exception {
        String html = IOUtils.toString(new URI(url));
        String text = ArticleExtractor.INSTANCE.getText(html);

        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);

        if (response.get("Resources") == null) {
            return null;
        }

        List<String> entities = new ArrayList<>();
        for (Map r : (List<Map>) response.get("Resources")) {
            entities.add((String) r.get("@URI"));
        }

        return entities;
    }

    public String getWebPageTitle(String uri) throws Exception{
        Document doc = Jsoup.connect(uri).get();

        if(doc == null || doc.select("title") == null) {
            return null;
        }

        return doc.select("title").html();
    }
}
