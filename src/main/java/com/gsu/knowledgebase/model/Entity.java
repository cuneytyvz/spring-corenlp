package com.gsu.knowledgebase.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Entity implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String dbpediaUri;
    private String wikidataId;
    private String wikipediaUri;
    private String entityType;
    private String webUri;
    private String image;
    private String smallImage;
    private String note;
    private Long categoryId = 1l;
    private Long subCategoryId = 1l;
    private String categoryName = "Other";
    private String subCategoryName = "All";
    private String source;
    private List<Property> properties = new ArrayList<>();
    private Long webPageEntityId;
    private List<Entity> annotationEntities = new ArrayList<>();

    public Entity() {
    }

    public Entity(ResultSet rs) throws SQLException {
        id = rs.getLong("e.id");
        name = rs.getString("e.name");
        description = rs.getString("e.description");
        shortDescription = rs.getString("e.short_description");
        dbpediaUri = rs.getString("e.dbpedia_uri");
        categoryId = rs.getLong("e.category_id");
        subCategoryId = rs.getLong("e.subcategory_id");
        wikidataId = rs.getString("e.wikidata_id");
        entityType = rs.getString("e.entity_type");
        webPageEntityId = rs.getLong("e.web_page_entity_id") == 0 ? null : rs.getLong("e.web_page_entity_id");
        webUri = rs.getString("e.web_uri");
        wikipediaUri = rs.getString("e.wikipedia_uri");
        note = rs.getString("e.note");
        image = rs.getString("e.image");
        smallImage = rs.getString("e.small_image");
        source = rs.getString("e.source");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDbpediaUri() {
        return dbpediaUri;
    }

    public void setDbpediaUri(String dbpediaUri) {
        this.dbpediaUri = dbpediaUri;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getWikidataId() {
        return wikidataId;
    }

    public void setWikidataId(String wikidataId) {
        this.wikidataId = wikidataId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getWebPageEntityId() {
        return webPageEntityId;
    }

    public void setWebPageEntityId(Long webPageEntityId) {
        this.webPageEntityId = webPageEntityId;
    }

    public List<Entity> getAnnotationEntities() {
        return annotationEntities;
    }

    public void setAnnotationEntities(List<Entity> annotationEntities) {
        this.annotationEntities = annotationEntities;
    }

    public String getWebUri() {
        return webUri;
    }

    public void setWebUri(String webUri) {
        this.webUri = webUri;
    }


    public String getWikipediaUri() {
        return wikipediaUri;
    }

    public void setWikipediaUri(String wikipediaUri) {
        this.wikipediaUri = wikipediaUri;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public Long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(Long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}