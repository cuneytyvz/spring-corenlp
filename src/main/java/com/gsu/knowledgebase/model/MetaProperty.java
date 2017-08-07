package com.gsu.knowledgebase.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cnytync on 22/03/2017.
 */
public class MetaProperty {
    private Long id;
    private String name;
    private String description;
    private String propertyType;
    private String datatype;
    private String source;
    private String uri;
    private Boolean visible;

    public MetaProperty() {
    }

    public MetaProperty(ResultSet rs) throws SQLException {
        id = rs.getLong("mp.id");
        name = rs.getString("mp.name");
        description = rs.getString("mp.description");
        uri = rs.getString("mp.uri");
        source = rs.getString("mp.source");
        propertyType = rs.getString("mp.property_type");
        datatype = rs.getString("mp.datatype");
        visible = rs.getBoolean("mp.visible");
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
