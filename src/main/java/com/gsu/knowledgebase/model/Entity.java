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
    private String dbpediaUri;
    private String wikidataId;
    private String entityType;
    private String webUri;
    private Long categoryId;
    private List<Property> properties = new ArrayList<>();
    private Long webPageEntityId;
    private List<Entity> annotationEntities = new ArrayList<>();

    public Entity() {
    }

    public Entity(ResultSet rs) throws SQLException {
        id = rs.getLong("e.id");
        name = rs.getString("e.name");
        description = rs.getString("e.description");
        dbpediaUri = rs.getString("e.dbpedia_uri");
        categoryId = rs.getLong("e.category_id");
        wikidataId = rs.getString("e.wikidata_id");
        entityType = rs.getString("e.entity_type");
        webPageEntityId = rs.getLong("e.web_page_entity_id");
        webUri = rs.getString("e.web_uri");
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
}