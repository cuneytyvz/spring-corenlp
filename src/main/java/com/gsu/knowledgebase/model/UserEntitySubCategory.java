package com.gsu.knowledgebase.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by cnytync on 02/03/2018.
 */
public class UserEntitySubCategory {
    private Long id;
    private Long userEntityId;
    private Long subcategoryId;

    public UserEntitySubCategory() {

    }

    public UserEntitySubCategory(ResultSet rs) throws SQLException {
        id = rs.getLong("ues.id");
        userEntityId = rs.getLong("ues.user_entity_id");
        subcategoryId = rs.getLong("ues.subcategory_id");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserEntityId() {
        return userEntityId;
    }

    public void setUserEntityId(Long userEntityId) {
        this.userEntityId = userEntityId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }
}
