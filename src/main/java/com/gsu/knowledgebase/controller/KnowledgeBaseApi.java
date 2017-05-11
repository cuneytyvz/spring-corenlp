package com.gsu.knowledgebase.controller;

import com.gsu.knowledgebase.model.Category;
import com.gsu.knowledgebase.model.Entity;
import com.gsu.knowledgebase.model.Property;
import com.gsu.knowledgebase.model.Subproperty;
import com.gsu.knowledgebase.repository.KnowledgeBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

/**
 * Created by cnytync on 21/12/14.
 */
@Controller("KnowledgeBaseApi")
@RequestMapping(value = "/knowledgeBase/api")
public class KnowledgeBaseApi {

    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;

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

        for (Property property : entity.getProperties()) {
            property.setEntityId(id);

            Long propertyId = knowledgeBaseDao.saveProperty(property);
            for(Subproperty subproperty : property.getSubproperties()) {
                subproperty.setPropertyId(propertyId);

                knowledgeBaseDao.saveSubproperty(subproperty);
            }
        }

        return id;
    }

    @RequestMapping(value = "/getAllEntities", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllEntities() throws Exception {
        Collection<Entity> entities = knowledgeBaseDao.findAllEntities();

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
