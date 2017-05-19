package com.gsu.knowledgebase.repository;

import com.gsu.common.util.DateUtils;
import com.gsu.common.util.MaxIdCalculator;
import com.gsu.knowledgebase.model.Category;
import com.gsu.knowledgebase.model.Entity;
import com.gsu.knowledgebase.model.Property;
import com.gsu.knowledgebase.model.Subproperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
public class KnowledgeBaseDao {

    @Autowired
    private MaxIdCalculator maxIdCalculator;

    @Autowired
    private DataSource kbDataSource;

    public Long saveEntity(Entity entity) throws Exception {

        String sql = "insert into entity set id = ?, name = ?, description = ?, dbpedia_uri = ?, wikidata_id = ?" +
                ", category_id = ?, cr_date = ?, entity_type = ?, web_page_entity_id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "entity", "id");

            ps.setLong(1, id);
            ps.setString(2, entity.getName());
            ps.setString(3, entity.getDescription());
            ps.setString(4, entity.getDbpediaUri());
            ps.setString(5, entity.getWikidataId());

            if (entity.getCategoryId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, entity.getCategoryId());
            }

            ps.setTimestamp(7, DateUtils.getCurrentTimeStamp());
            ps.setString(8, entity.getEntityType());
            if (entity.getWebPageEntityId() == null) {
                ps.setNull(9, Types.BIGINT);
            } else {
                ps.setLong(9, entity.getWebPageEntityId());
            }
            ps.execute();

            ps.close();

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Long saveProperty(Property property) {

        String sql = "insert into PROPERTY set id = ?, description = ?, name = ?, lang = ?, value = ?, " +
                " uri = ?, datatype = ?, source = ?, entity_id = ?, value_label = ?, property_type = ?";

        Connection conn = null;

//        Property p={id:propId,name:propName};

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "property", "id");

            ps.setLong(1, id);
            ps.setString(2, property.getDescription());
            ps.setString(3, property.getName());
            ps.setString(4, property.getLang());
            ps.setString(5, property.getValue());
            ps.setString(6, property.getUri());
            ps.setString(7, property.getDatatype());
            ps.setString(8, property.getSource());
            ps.setLong(9, property.getEntityId());
            ps.setString(10, property.getValueLabel());
            ps.setString(11, property.getPropertyType());

            ps.execute();

            ps.close();

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void saveProperties(List<Property> properties) {

        String sql = "insert into PROPERTY set id = ?, description = ?, name = ?, lang = ?, value = ?, " +
                " uri = ?, datatype = ?, source = ?, entity_id = ?, value_label = ?, property_type = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            for (Property property : properties) {
                Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "property", "id");
                property.setId(id);

                ps.setLong(1, id);
                ps.setString(2, property.getDescription());
                ps.setString(3, property.getName());
                ps.setString(4, property.getLang());
                ps.setString(5, property.getValue());
                ps.setString(6, property.getUri());
                ps.setString(7, property.getDatatype());
                ps.setString(8, property.getSource());
                ps.setLong(9, property.getEntityId());
                ps.setString(10, property.getValueLabel());
                ps.setString(11, property.getPropertyType());

                ps.execute();
            }

            ps.close();

            return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Long saveSubproperty(Subproperty property) {

        String sql = "insert into subproperty set id = ?, property_id = ?, name = ?, value = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "subproperty", "id");

            ps.setLong(1, id);
            ps.setLong(2, property.getPropertyId());
            ps.setString(3, property.getName());
            ps.setString(4, property.getValue());

            ps.execute();

            ps.close();

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void saveSubproperties(List<Subproperty> subproperties) {

        String sql = "insert into subproperty set id = ?, property_id = ?, name = ?, value = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            for (Subproperty subproperty : subproperties) {
                Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "subproperty", "id");
                subproperty.setId(id);

                ps.setLong(1, id);
                ps.setLong(2, subproperty.getPropertyId());
                ps.setString(3, subproperty.getName());
                ps.setString(4, subproperty.getValue());

                ps.execute();
            }

            ps.close();

            return;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Collection<Entity> findAllEntities() {
        String sql = "select * from entity e left join property pr on pr.entity_id = e.id " +
                " where entity_type <> 'web-page-annotation';";

        Connection conn = null;

        HashMap<Long, Entity> map = new HashMap<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Long entityId = rs.getLong("e.id");

                Entity entity;
                if (!map.containsKey(entityId)) {
                    entity = new Entity(rs);

                    map.put(entityId, entity);
                } else {
                    entity = map.get(entityId);
                }

                Property property = new Property(rs);
                entity.getProperties().add(property);
            }

            rs.close();
            ps.close();

            return map.values();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Long saveCategory(String name) {

        String sql = "insert into category set id = ?, name = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "category", "id");

            ps.setLong(1, id);
            ps.setString(2, name);

            ps.execute();

            ps.close();

            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Collection<Category> findAllCategories() {
        String sql = "select * from category c;";

        Connection conn = null;

        List<Category> categories = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                categories.add(new Category(rs));
            }

            rs.close();
            ps.close();

            return categories;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public Entity findEntityByName(String name) {
        String sql = "select * from entity e left join property pr on pr.entity_id = e.id where e.name = ?;";

        Connection conn = null;

        Entity entity = null;
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entity = new Entity(rs);

                Property property = new Property(rs);
                entity.getProperties().add(property);
            }

            rs.close();
            ps.close();

            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

}
