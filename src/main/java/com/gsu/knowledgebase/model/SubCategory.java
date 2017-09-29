package com.gsu.knowledgebase.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SubCategory implements Serializable {
    private Long id;
    private Long categoryId;
    private String name;
    private List<Entity> properties = new ArrayList<>();

    public SubCategory() {
    }

    public SubCategory(ResultSet rs) throws SQLException {
        id = rs.getLong("sc.id");
        categoryId = rs.getLong("sc.category_id");
        name = rs.getString("sc.name");
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

    public List<Entity> getProperties() {
        return properties;
    }

    public void setProperties(List<Entity> properties) {
        this.properties = properties;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}


