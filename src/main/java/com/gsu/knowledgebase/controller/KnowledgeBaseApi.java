package com.gsu.knowledgebase.controller;

import com.gsu.common.util.ImageUtils;
import com.gsu.knowledgebase.model.*;
import com.gsu.knowledgebase.repository.KnowledgeBaseDao;
import com.gsu.knowledgebase.service.DBPedia;
import com.gsu.knowledgebase.service.DBpediaSpotlight;
import com.gsu.knowledgebase.service.MailService;
import com.gsu.knowledgebase.service.WebPageAnnotator;
import com.gsu.knowledgebase.util.Constants;
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

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/signup", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object signup(@RequestBody final User user) throws Exception {
        User u = knowledgeBaseDao.findUserByUsername(user.getUsername());
        if (u != null) {
            return -1;
        }

        u = knowledgeBaseDao.findUserByEmail(user.getEmail());
        if (u != null) {
            return -2;
        }

        user.setRoleId(Constants.ROLE_USER);
        user.setStatus(Constants.USER_STATUS_AWAITING_CONFIRMATION);

        // Save User
        Long userId = knowledgeBaseDao.saveUser(user);

        // Create confirmation code and save
        UUID uuid = UUID.randomUUID();

        UserConfirmation uc = new UserConfirmation();
        uc.setStatus(Constants.USER_CONFIRMATION_STATUS_AWAITING);
        uc.setUserId(userId);
        uc.setConfirmationCode(uuid.toString());

        knowledgeBaseDao.saveUserConfirmation(uc);

        // Send confirmation mail
        Mail mail = new Mail();
        mail.setMailTo(user.getEmail());
        mail.setMailSubject("Confirmation Email");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstName", user.getFirstName());
        model.put("lastName", user.getLastName());
        model.put("confirmationCode", uc.getConfirmationCode());

        mail.setModel(model);

        mailService.sendEmail(mail, "/templates/confirmation.vm");

        return user;
    }

    @RequestMapping(value = "/confirmUser/{confirmationCode}", method = RequestMethod.GET)
    public
    String confirmUser(@PathVariable String confirmationCode) throws Exception {
        UserConfirmation uc = knowledgeBaseDao.findAwaitingUserConfirmationByCode(confirmationCode);

        if (uc == null) {
            return "redirect:/knowledge-base/confirmation-code-not-found";
        }

        // Update user and confirmation status
        knowledgeBaseDao.updateUserStatus(uc.getUserId(), Constants.USER_STATUS_CONFIRMED);
        knowledgeBaseDao.updateUserConfirmationStatus(uc.getId(), Constants.USER_CONFIRMATION_STATUS_CONFIRMED);

        User user = knowledgeBaseDao.findUserById(uc.getUserId());

        // Send registration mail
        Mail mail = new Mail();
        mail.setMailTo(user.getEmail());
        mail.setMailSubject("Registration Completed");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("firstName", user.getFirstName());
        model.put("lastName", user.getLastName());

        mail.setModel(model);

        mailService.sendEmail(mail, "/templates/registration.vm");

        return  "redirect:/knowledge-base/user-confirmed";
    }

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

    @RequestMapping(value = "/unexpectedError", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object unexpectedError(@RequestBody UnexpectedError error) throws Exception {
        // TODO save unexpected error log

        return 0;
    }

    @RequestMapping(value = "/saveCustomProperty", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object saveCustomProperty(@RequestBody CustomProperty customProperty) throws Exception {
        Entity s = customProperty.getSubject();
        MetaProperty mp = customProperty.getPredicate();
        Entity o = customProperty.getObject();

        if (mp.getId() == null) {
            mp.setVisibility(3);
            Long id = knowledgeBaseDao.saveMetaProperty(mp);
            mp.setId(id);
        }

        if (o.getId() == null) {
            Entity e = knowledgeBaseDao.findEntityByDbpediaUriWikidataId(o);

            if (e == null) {
                o.setSource("custom");
                Long id = knowledgeBaseDao.saveEntity(o);

                o.setId(id);
            } else {
                customProperty.setObject(e);
            }
        }

        Property p = new Property(mp);
        p.setSource("custom");

        if (o.getEntityType().equals("semantic-web")) {
            if (o.getDbpediaUri() != null && o.getDbpediaUri().length() > 0) {
                p.setValue(o.getDbpediaUri());
            } else {
                p.setValue(o.getWikidataId());
            }
        } else { // custom entity
            p.setValue(o.getName());
            p.setCustomEntityId(o.getId());
        }

        p.setEntityId(customProperty.getSubject().getId());
        p.setValueLabel(o.getName());

        // if such a triple does not exists then save...
        List<Property> results = knowledgeBaseDao.findPropertiesByTriples(s.getId(), p.getMetaPropertyId(), p.getValueLabel());
        if (results == null || results.size() == 0) {
            Long id = knowledgeBaseDao.saveProperty(p);
            p.setId(id);
        } else {
            p.setId(results.get(0).getId());
        }

        return p;
    }

    @RequestMapping(value = "/saveCustomProperties", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    void saveCustomProperties(@RequestBody CustomProperty[] customProperties) throws Exception {
        for (CustomProperty customProperty : customProperties) {
            Entity s = customProperty.getSubject();
            MetaProperty mp = customProperty.getPredicate();
            Entity o = customProperty.getObject();

            if (mp.getId() == null) {
                mp.setVisibility(3);
                Long id = knowledgeBaseDao.saveMetaProperty(mp);
                mp.setId(id);
            }

            if (o.getId() == null) {
                Entity e = knowledgeBaseDao.findEntityByDbpediaUriWikidataId(o);

                if (e == null) {
                    o.setSource("custom");
                    Long id = knowledgeBaseDao.saveEntity(o);

                    o.setId(id);
                } else {
                    customProperty.setObject(e);
                }
            }

            Property p = new Property(mp);
            p.setSource("custom");

            if (o.getEntityType().equals("semantic-web")) {
                if (o.getDbpediaUri() != null && o.getDbpediaUri().length() > 0) {
                    p.setValue(o.getDbpediaUri());
                } else {
                    p.setValue(o.getWikidataId());
                }
            } else { // custom entity
                p.setValue(o.getName());
                p.setCustomEntityId(o.getId());
            }

            p.setEntityId(customProperty.getSubject().getId());
            p.setValueLabel(o.getName());

            // if such a triple does not exists then save...
            List<Property> results = knowledgeBaseDao.findPropertiesByTriples(s.getId(), p.getMetaPropertyId(), p.getValueLabel());
            if (results == null || results.size() == 0) {
                Long id = knowledgeBaseDao.saveProperty(p);
                p.setId(id);
            } else {
                p.setId(results.get(0).getId());
            }
        }


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
            return -1;
        }

        if (entity.getEntityType().equals("web-page")) {
            try {
                entity.setWebPageText(webPageAnnotator.getWebPageTextAsHtml(entity.getWebUri()));
                entity.setDescription(entity.getWebPageText());

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

        // commons.wikimedia redirect problem workaround
        if (entity.getImage() != null && entity.getImage().contains("commons.wikimedia")) {
            entity.setImage(entity.getSecondaryImage());
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

        if (entity.getSecondaryImage() != null && !entity.getSecondaryImage().isEmpty()) {
            BufferedImage bf = imageUtils.readImageFromUri(entity.getSecondaryImage());

            if (bf != null) {
                String filename = imageUtils.saveJpegPngImage(bf, entity.getName().replace(" ", "_") + "_" + entity.getWikidataId() + "_secondary");
                entity.setSecondaryImage(filename);

                BufferedImage scaledImage = imageUtils.getScaledImage(bf, ImageUtils.SCALED_IMAGE_WIDTH, ImageUtils.SCALED_IMAGE_HEIGHT);
                String smallFilename = imageUtils.saveScaledJpegPngImage(scaledImage, entity.getName() + "_" + entity.getWikidataId() + "_secondary");
                entity.setSmallSecondaryImage(smallFilename);
            }
        }


        final Long id = knowledgeBaseDao.saveEntity(entity);
        entity.setId(id);

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
        for (Property property : entity.getProperties()) {
            for (Subproperty subproperty : property.getSubproperties()) {
                subproperty.setPropertyId(property.getId());
                subproperties.add(subproperty);
            }
        }

        knowledgeBaseDao.saveSubproperties(subproperties);

        final ExecutorService executor2 = Executors.newSingleThreadExecutor();
        executor2.execute(new Runnable() {
            @Override
            public void run() {
                Collection<AnnotationItem> items = dbpediaSpotlight.annotateText(entity.getDescription());

                for (AnnotationItem i : items) {
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
    Object getAnnotationEntities(@PathVariable("entityId") Integer entityId) throws Exception {
        Collection<AnnotationItem> annotationItems = knowledgeBaseDao.findAnnotationItems(entityId);

//        for (AnnotationItem item : annotationItems) {
//            if (e.getEntityType().equals("web-page")) {
//                e.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(e.getId()));
//            }
//        }

        return annotationItems;
    }

    @RequestMapping(value = "/getAnnotationEntitiesByText", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAnnotationEntitiesByText(@RequestBody AnnotationRequest text) throws Exception {
        Collection<AnnotationItem> items = dbpediaSpotlight.annotateText(text.getText());

        return items;
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

    @RequestMapping(value = "/getMainPageEntityList", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getEntitgetMainPageEntityListyList() throws Exception {
        List<Entity> entities = new ArrayList<>(knowledgeBaseDao.findAllEntitiesLazy());

//        for (Entity e : entities) {
//            if (e.getEntityType().equals("web-page")) {
//                e.getAnnotationEntities().addAll(knowledgeBaseDao.findAnnotationEntities(e.getId()));
//            }
//        }

        List<Entity> randomEntities = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            int index = r.nextInt(entities.size());
            randomEntities.add(entities.get(index));
        }

        return randomEntities;
    }

    @RequestMapping(value = "/addEntityToCategory", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object addEntityToCategory(@RequestParam Long entityId, @RequestParam Long categoryId) throws Exception {
        knowledgeBaseDao.addEntityToCategory(entityId, categoryId);

        return null;
    }

    @RequestMapping(value = "/addEntityToSubCategory", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object addEntityToSubCategory(@RequestParam Long entityId, @RequestParam Long subCategoryId) throws Exception {
        knowledgeBaseDao.addEntityToSubCategory(entityId, subCategoryId);

        return null;
    }

    @RequestMapping(value = "/removeEntityFromCategory", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object removeEntityFromCategory(@RequestParam Long entityId) throws Exception {
        knowledgeBaseDao.removeEntityFromCategory(entityId);

        return null;
    }

    @RequestMapping(value = "/removeEntityFromSubCategory", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object removeEntityFromSubCategory(@RequestParam Long entityId) throws Exception {
        knowledgeBaseDao.removeEntityFromSubCategory(entityId);

        return null;
    }

    @RequestMapping(value = "/saveCategory/{name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object addCategory(@PathVariable String name) throws Exception {
        return knowledgeBaseDao.saveCategory(name);
    }

    @RequestMapping(value = "/saveSubcategory/{categoryId}/{name}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object addSubcategory(@PathVariable Long categoryId, @PathVariable String name) throws Exception {
        return knowledgeBaseDao.saveSubcategory(categoryId, name);
    }

    @RequestMapping(value = "/autocompleteProperty/{prefix}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object autocompleteProperty(@PathVariable("prefix") String prefix) throws Exception {
        Collection<MetaProperty> metaProperties = knowledgeBaseDao.findPropertiesByPrefix(prefix);

        List<MetaProperty> withoutSameAs = new ArrayList<>();

        for (MetaProperty mp1 : metaProperties) {
            if (mp1.getSameAs() == null || mp1.getSameAs() == 0) {
                withoutSameAs.add(mp1);
            } else {
                boolean sameAsExists = false;
                for (MetaProperty mp2 : metaProperties) {
                    if (mp2.getSameAs() != null && mp1.getSameAs().equals(mp2.getSameAs())) {
                        sameAsExists = true;
                    }
                }

                if (!sameAsExists) {
                    withoutSameAs.add(mp1);
                }
            }
        }

        return withoutSameAs;
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