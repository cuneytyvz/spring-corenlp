package com.gsu.knowledgebase.controller;

import com.gsu.knowledgebase.model.Category;
import com.gsu.knowledgebase.model.Entity;
import com.gsu.knowledgebase.model.Property;
import com.gsu.knowledgebase.model.Subproperty;
import com.gsu.knowledgebase.repository.KnowledgeBaseDao;
import com.gsu.knowledgebase.service.DBPedia;
import com.gsu.knowledgebase.service.WebPageAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller("KnowledgeBaseApi")
@RequestMapping(value = "/knowledgeBase/api")
public class KnowledgeBaseApi {

    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;

    @Autowired
    private WebPageAnnotator webPageAnnotator;

    @Autowired
    private DBPedia dbPedia;

    @RequestMapping(value = "/saveEntity", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object saveEntity(@RequestBody Entity entity) throws Exception {
        Entity pl = knowledgeBaseDao.findEntityByName(entity.getName());
        if (pl != null) {
            return null;
        }

        String title = webPageAnnotator.getWebPageTitle(entity.getWebUri());
        entity.setName(title);

        Long id = knowledgeBaseDao.saveEntity(entity);


        if (entity.getEntityType().equals("web-page")) {
            List<String> uris = webPageAnnotator.annotateWebPage(entity.getWebUri());

            Map<String, Entity> webPageEntities = new HashMap<>();
            for (String dbpediaUri : uris) {
                try {
                    Entity webPageEntity = dbPedia.getEntityByUri(dbpediaUri);
                    webPageEntity.setWebPageEntityId(id);

                    webPageEntities.put(webPageEntity.getName(), webPageEntity);
                } catch (Exception e) {
                    System.err.println("Error at dbPedia.getEntityByUri");
                    e.printStackTrace();
                }
            }

            for (String key : webPageEntities.keySet()) {
                Entity webPageEntity = webPageEntities.get(key);

                Long webPageEntityId = knowledgeBaseDao.saveEntity(webPageEntity);
                for (Property property : webPageEntity.getProperties()) {
                    property.setEntityId(webPageEntityId);
                }

                knowledgeBaseDao.saveProperties(webPageEntity.getProperties());

                List<Subproperty> subproperties = new ArrayList<>();
                for (Property property : webPageEntity.getProperties()) {
                    for (Subproperty subproperty : property.getSubproperties()) {
                        subproperty.setPropertyId(property.getId());
                        subproperties.add(subproperty);
                    }
                }

                knowledgeBaseDao.saveSubproperties(subproperties);
            }
        }

        for (Property property : entity.getProperties()) {
            property.setEntityId(id);
        }

        knowledgeBaseDao.saveProperties(entity.getProperties());

        List<Subproperty> subproperties = new ArrayList<>();
        for (Property property : entity.getProperties()) {
            for (Subproperty subproperty : property.getSubproperties()) {
                subproperty.setPropertyId(property.getId());
                subproperties.add(subproperty);
            }
        }

        knowledgeBaseDao.saveSubproperties(subproperties);

        return id;
    }

    @RequestMapping(value = "/getAllEntities", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllEntities() throws Exception {
        Collection<Entity> entities = knowledgeBaseDao.findAllEntities();

        for (Entity e : entities) {
            if (e.getEntityType().equals("web-page")) {
                e.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(e.getId()));
            }
        }

        return entities;
    }

    @RequestMapping(value = "/getAllCategories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllCategories() throws Exception {
        Collection<Category> categories = knowledgeBaseDao.findAllCategories();

        return categories;
    }
}
