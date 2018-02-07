package com.gsu.knowledgebase.controller;

import com.gsu.common.util.ImageUtils;
import com.gsu.knowledgebase.model.*;
import com.gsu.knowledgebase.repository.KnowledgeBaseDao;
import com.gsu.knowledgebase.service.DBPedia;
import com.gsu.knowledgebase.service.DBpediaSpotlight;
import com.gsu.knowledgebase.service.WebPageAnnotator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller("KnowledgeBaseApi")
@RequestMapping(value = "/knowledgeBase/api")
public class KnowledgeBaseApi {

    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;

    @Autowired
    private WebPageAnnotator webPageAnnotator;

    @Autowired
    private DBPedia dbPedia;

    @Autowired
    private ImageUtils imageUtils;

    @Autowired
    private DBpediaSpotlight dbpediaSpotlight;

    @Autowired
    private ServletContext servletContext;

    @ResponseBody
    @RequestMapping(value = "/image/{image:.*}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@PathVariable("image") String image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        File f = new File(ImageUtils.KB_IMG_LOCATION + image);
        try {
            InputStream fis = new FileInputStream(f);
            byte[] bytes = IOUtils.toByteArray(fis);
            fis.close();

            return bytes;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @RequestMapping(value = "/updateEntity", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object updateEntity(@RequestBody Entity entity) throws Exception {
        Long id = knowledgeBaseDao.updateEntity(entity);

        return id;
    }

    @RequestMapping(value = "/removeEntity/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object removeEntity(@PathVariable Long id) throws Exception {
        knowledgeBaseDao.removeEntity(id);

        return id;
    }

    @RequestMapping(value = "/saveEntity", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object saveEntity(@RequestBody final Entity entity) throws Exception {
        Entity pl = knowledgeBaseDao.findEntityByName(entity.getName());
        if (pl != null && !pl.getEntityType().equals("web-page-annotation")) {
            return null;
        }

        if (entity.getEntityType().equals("web-page")) {
            try {
                String title = webPageAnnotator.getWebPageTitle(entity.getWebUri());
                if (title == null || title.isEmpty())
                    entity.setName(entity.getWebUri());
                else
                    entity.setName(title);

                entity.setImage(webPageAnnotator.getWebsiteImage(entity.getWebUri()));
            } catch (Exception e) {
                entity.setName(entity.getWebUri());
                e.printStackTrace();
            }
        }

        if (entity.getImage() != null && !entity.getImage().isEmpty()) {
            BufferedImage bf = imageUtils.readImageFromUri(entity.getImage());

            if (bf != null) {
                String filename = imageUtils.saveJpegPngImage(bf, entity.getName().replace(" ", "_") + "_" + entity.getWikidataId());
                entity.setImage(filename);

                BufferedImage scaledImage = imageUtils.getScaledImage(bf, ImageUtils.SCALED_IMAGE_WIDTH, ImageUtils.SCALED_IMAGE_HEIGHT);
                String smallFilename = imageUtils.saveScaledJpegPngImage(scaledImage, entity.getName() + "_" + entity.getWikidataId());
                entity.setSmallImage(smallFilename);
            }
        }

        Long id = knowledgeBaseDao.saveEntity(entity);
        entity.setId(id);

        if (entity.getEntityType().equals("web-page")) {
            List<String> uris = webPageAnnotator.annotateWebPage(entity.getWebUri());

            Map<String, Entity> webPageEntities = new HashMap<>();
            for (String dbpediaUri : uris) {
                try {
                    if (webPageEntities.get(dbpediaUri) == null) {
                        Entity webPageEntity = dbPedia.getEntityByUri(dbpediaUri);
                        webPageEntity.setWebPageEntityId(id);

                        webPageEntities.put(webPageEntity.getDbpediaUri(), webPageEntity);
                    } else {
                        // do nothing...
                    }

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
        for (Property property : entity.getProperties())
        {
            for (Subproperty subproperty : property.getSubproperties()) {
                subproperty.setPropertyId(property.getId());
                subproperties.add(subproperty);
            }
        }

        knowledgeBaseDao.saveSubproperties(subproperties);

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Collection<AnnotationItem> items = dbpediaSpotlight.annotateText(entity.getDescription());

                for(AnnotationItem i : items) {
                    i.setReferencedEntityId(entity.getId());
                }

                try {
                    knowledgeBaseDao.saveAnnotationItems(new ArrayList<AnnotationItem>(items));
                } catch (Exception e) {
                    System.err.println("ERROR at dbpediaspotlight, saving annotation items...");
                    e.printStackTrace();
                }
            }
        });


        return entity;
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

    @RequestMapping(value = "/getAnnotationEntities/{entityId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAnnotationEntities(@PathVariable("entityId") Integer entityId ) throws Exception {
        Collection<AnnotationItem> annotationItems = knowledgeBaseDao.findAnnotationItems(entityId);

//        for (AnnotationItem item : annotationItems) {
//            if (e.getEntityType().equals("web-page")) {
//                e.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(e.getId()));
//            }
//        }

        return annotationItems;
    }

    @RequestMapping(value = "/getEntityList", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getEntityList() throws Exception {
        Collection<Entity> entities = knowledgeBaseDao.findAllEntitiesLazy();

//        for (Entity e : entities) {
//            if (e.getEntityType().equals("web-page")) {
//                e.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(e.getId()));
//            }
//        }

        return entities;
    }

    @RequestMapping(value = "/getEntityById/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getEntityById(@PathVariable(value = "id") Long id) throws Exception {
        Entity entity = knowledgeBaseDao.findEntityById(id);

        if (entity.getEntityType().equals("web-page")) {
            entity.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(entity.getId()));
        }

        return entity;
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

    @RequestMapping(value = "/getSubCategories/{categoryId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getSubCategories(@PathVariable Long categoryId) throws Exception {
        Collection<SubCategory> subCategories = knowledgeBaseDao.findSubCategories(categoryId);

        return subCategories;
    }

    @RequestMapping(value = "/getAllSubCategories", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllSubCategories() throws Exception {
        Collection<SubCategory> subCategories = knowledgeBaseDao.findAllSubCategories();

        return subCategories;
    }
}