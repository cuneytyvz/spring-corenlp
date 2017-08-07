package com.gsu.knowledgebase.controller;

import com.gsu.knowledgebase.model.*;
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

        Long id = knowledgeBaseDao.saveEntity(entity);

        if (entity.getEntityType().equals("web-page")) {
            List<String> uris = webPageAnnotator.annotateWebPage(entity.getWebUri());

            try {
                String title = webPageAnnotator.getWebPageTitle(entity.getWebUri());
                entity.setName(title);
            } catch (Exception e) {
                entity.setName(entity.getWebUri());
                e.printStackTrace();
            }


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

                // save to metaproperty if not exists before
                // fetch from metaproperty
                // if not exists there, save this prop to metaproperty first
                for (Property p : webPageEntity.getProperties()) {
                    MetaProperty mp = knowledgeBaseDao.findMetapropertyByUri(p.getUri());
                    if (mp == null) {
                        Long mpId = knowledgeBaseDao.saveMetaProperty(p);
                        p.setMetaPropertyId(mpId);
                    } else {
                        p.setMetaPropertyId(mp.getId());
                    }
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

            MetaProperty mp = knowledgeBaseDao.findMetapropertyByUri(property.getUri());
            if (mp == null) {
                Long mpId = knowledgeBaseDao.saveMetaProperty(property);
                property.setMetaPropertyId(mpId);
            } else {
                property.setMetaPropertyId(mp.getId());
            }

            if (property.getValueLabel() != null && property.getValueLabel().isEmpty()) {
                property.setValueLabel(property.getValue());
            }
        }

        // save to metaproperty if not exists before
        knowledgeBaseDao.saveProperties(entity.getProperties());

        List<Subproperty> subproperties = new ArrayList<>();
        for (
                Property property
                : entity.getProperties())

        {
            for (Subproperty subproperty : property.getSubproperties()) {
                subproperty.setPropertyId(property.getId());
                subproperties.add(subproperty);
            }
        }

        knowledgeBaseDao.saveSubproperties(subproperties);

        return id;
    }

    @RequestMapping(value = "/transferMetaprops", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object transferMetaprops() throws Exception {
        Collection<MetaProperty> metaProperties = knowledgeBaseDao.findAllMetaproperties();

        Collection<Property> properties = knowledgeBaseDao.findAllProperties();

        List<Property> propsToTransfer = new ArrayList<>();
        for (Property p : properties) {

            boolean exists = false;
            for (MetaProperty mp : metaProperties) {
                if (mp.getUri() != null && mp.getUri().equals(p.getUri())) {
                    exists = true;
                }
            }

            for (Property mp : propsToTransfer) {
                if (mp.getUri() != null && mp.getUri().equals(p.getUri())) {
                    exists = true;
                }
            }

            if (!exists && p.getUri() != null) {
                propsToTransfer.add(p);
            }
        }

        knowledgeBaseDao.transferPropsToMetaProps(propsToTransfer);

        return "Successful";
    }

    @RequestMapping(value = "/matchPropsToMetaprops", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object matchPropsToMetaprops() throws Exception {
        Collection<MetaProperty> metaProperties = knowledgeBaseDao.findAllMetaproperties();

        List<Property> properties = knowledgeBaseDao.findAllProperties();

        for (Property p : properties) {
            for (MetaProperty mp : metaProperties) {
                if (p.getUri() != null && mp.getUri() != null && p.getUri().equals(mp.getUri())) {
                    p.setMetaPropertyId(mp.getId());
                }
            }
        }

        knowledgeBaseDao.updateProperties(properties);

        return "Successful";
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

    @RequestMapping(value = "/getAllMetaproperties", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllMetaproperties() throws Exception {
        Collection<MetaProperty> metaProperties = knowledgeBaseDao.findAllMetaproperties();

        return metaProperties;
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
