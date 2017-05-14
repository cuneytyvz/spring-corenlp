package com.gsu.knowledgebase.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gsu.knowledgebase.model.Entity;
import com.gsu.knowledgebase.model.Property;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class DBPedia {

    public static void main(String args[]) throws Exception {
        new DBPedia().getEntityByUri("http://dbpedia.org/resource/Bayesian_network");
    }

    public Entity getEntityByUri(String uri) throws Exception {
        Entity entity = new Entity();

        String req = "http://live.dbpedia.org/sparql?default-graph-uri=" +
                "http%3A%2F%2Fdbpedia.org&query=DESCRIBE%20" +
                "%3C" + uri + "%3E" +
                "&" +
                "format=application%2Fjson-ld";

        String str = IOUtils.toString(new URI(req));
        JsonObject json = new JsonParser().parse(str).getAsJsonObject();

        JsonObject item = json.getAsJsonObject(uri);

        JsonArray arr = item.getAsJsonArray("http://dbpedia.org/ontology/abstract");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.get(i).getAsJsonObject();
            if (getAsString(o, "lang").equals("en")) {
                entity.setDescription(getAsString(o, "value"));
            }
        }

        arr = item.getAsJsonArray("http://www.w3.org/2000/01/rdf-schema#label");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                if (getAsString(o, "lang").equals("en")) {
                    entity.setName(getAsString(o, "value"));
                }
            }
        }

        List<Property> properties = new ArrayList<>();
        for (Map.Entry<String, JsonElement> propertyElement : item.entrySet()) {
            arr = propertyElement.getValue().getAsJsonArray();

            Iterator<JsonElement> it = arr.iterator();
            while (it.hasNext()) {
                JsonObject property = it.next().getAsJsonObject();

                Property p = new Property();
                p.setSource("dbpedia");
                p.setName(getTypeNameFromUri(propertyElement.getKey()));
                p.setUri(propertyElement.getKey());
                p.setLang(getAsString(property, "en"));
                p.setDatatype(getAsString(property, "datatype"));
                p.setType(getAsString(property, "type"));
                p.setValue(getAsString(property, "value"));

                properties.add(p);
            }
        }

        entity.setProperties(properties);
        entity.setEntityType("web-page-annotation");
        entity.setDbpediaUri(uri);

        return entity;
    }

    private String getTypeNameFromUri(String uri) {
        if (uri.contains("w3.org")) {
            return uri.split("#")[1];
        } else {
            String[] arr = uri.split("/");
            return arr[arr.length - 1];
        }
    }

    private String getAsString(JsonObject o, String propertyName) {
        if (o.getAsJsonPrimitive(propertyName) == null) {
            return null;
        } else {
            return o.getAsJsonPrimitive(propertyName).getAsString();
        }
    }

}
