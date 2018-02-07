package com.gsu.knowledgebase.service;

import com.gsu.knowledgebase.model.AnnotationItem;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class DBpediaSpotlight {

    public Collection<AnnotationItem> annotateText(String text) {
        Map response = new RestTemplate().getForObject(
                "http://model.dbpedia-spotlight.org/en/annotate?text=" +
                        text +
                        "&confidence=0.5&support=0&spotter=Default&disambiguator=Default&policy=whitelist&types=&sparql="
                , Map.class);

        Map<String, AnnotationItem> items = new HashMap<>();
        for (Map r : (List<Map>) response.get("Resources")) {
            AnnotationItem item = new AnnotationItem();
            item.setOffset(Integer.parseInt((String) r.get("@offset")));
            item.setUri((String) r.get("@URI"));
            item.setSurfaceForm((String) r.get("@surfaceForm"));
            item.setTypes((String) r.get("@types"));

            if (!items.containsKey(item.getUri())) {
                items.put(item.getUri(), item);
            }
        }

        return items.values();
    }
}
