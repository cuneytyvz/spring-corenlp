package com.gsu.knowledgebase.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Category implements Serializable {
    private Long id;
    private String name;
    private List<Entity> properties = new ArrayList<>();

    public Category() {
    }

    public Category(ResultSet rs) throws SQLException {
        id = rs.getLong("c.id");
        name = rs.getString("c.name");
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
}
