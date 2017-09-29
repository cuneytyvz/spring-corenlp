package com.gsu.knowledgebase.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DBpediaSpotlight {

    public List<String> annotateText(String text) {
        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);

        List<String> entities = new ArrayList<>();
        for (Map r : (List<Map>) response.get("Resources")) {
            String uri = (String) r.get("@URI");

            if(!entities.contains(uri)) {
                entities.add(uri);
            }

        }

        return entities;
    }
}
