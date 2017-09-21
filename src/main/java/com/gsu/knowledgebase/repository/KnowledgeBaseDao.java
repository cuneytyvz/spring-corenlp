package com.gsu.knowledgebase.repository;

import com.gsu.common.util.DateUtils;
import com.gsu.common.util.MaxIdCalculator;
import com.gsu.knowledgebase.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Component
public class KnowledgeBaseDao {

    @Autowired
    private MaxIdCalculator maxIdCalculator;

    @Autowired
    private DataSource kbDataSource;

    public Long saveEntity(Entity entity) throws Exception {

        String sql = "insert into entity set id = ?, name = ?, description = ?, dbpedia_uri = ?, wikidata_id = ?" +
                ", category_id = ?, cr_date = ?, entity_type = ?, web_page_entity_id = ?, web_uri = ?, image = ?," +
                " wikipedia_uri = ?,short_description = ?, small_image = ?, note = ?";

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

            ps.setString(10, entity.getWebUri());
            ps.setString(11, entity.getImage());
            ps.setString(12, entity.getWikipediaUri());
            ps.setString(13, entity.getShortDescription());
            ps.setString(14, entity.getSmallImage());
            ps.setString(15, entity.getNote());
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

    public Long updateEntity(Entity entity) throws Exception {

        String sql = "update entity set name = ?, description = ?, dbpedia_uri = ?, wikidata_id = ?" +
                ", category_id = ?, cr_date = ?, entity_type = ?, web_page_entity_id = ?, web_uri = ?, image = ?, " +
                " wikipedia_uri = ?,short_description = ?, small_image = ?, note = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "entity", "id");

            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setString(3, entity.getDbpediaUri());
            ps.setString(4, entity.getWikidataId());

            if (entity.getCategoryId() == null) {
                ps.setNull(5, Types.BIGINT);
            } else {
                ps.setLong(5, entity.getCategoryId());
            }

            ps.setTimestamp(6, DateUtils.getCurrentTimeStamp());
            ps.setString(7, entity.getEntityType());
            if (entity.getWebPageEntityId() == null) {
                ps.setNull(8, Types.BIGINT);
            } else {
                ps.setLong(8, entity.getWebPageEntityId());
            }

            ps.setString(9, entity.getWebUri());
            ps.setString(10, entity.getImage());
            ps.setString(11, entity.getWikipediaUri());
            ps.setString(12, entity.getShortDescription());
            ps.setString(13, entity.getSmallImage());
            ps.setString(14, entity.getNote());
            ps.setLong(15, entity.getId());

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
                " uri = ?, datatype = ?, source = ?, entity_id = ?, value_label = ?, property_type = ?, meta_property_id = ?";

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
                ps.setLong(12, property.getMetaPropertyId());

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

    public void updateProperties(List<Property> properties) {

        String sql = "update property set meta_property_id = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            for (Property property : properties) {

                ps.setLong(1, property.getMetaPropertyId());
                ps.setLong(2, property.getId());

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

    public Long ntiSubproperty(Subproperty property) {

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

    public void transferPropsToMetaProps(List<Property> properties) {

        String sql = "insert into meta_property set id = ?, name = ?, uri = ?, source = ?, description = ?, property_type = ?, datatype = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            for (Property property : properties) {
                Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "meta_property", "id");
                property.setId(id);

                ps.setLong(1, id);
                ps.setString(2, property.getName());
                ps.setString(3, property.getUri());
                ps.setString(4, property.getSource());
                ps.setString(5, property.getDescription());
                ps.setString(6, property.getPropertyType());
                ps.setString(7, property.getDatatype());

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

    public Long saveMetaProperty(Property property) {

        String sql = "insert into meta_property set id = ?, name = ?, uri = ?, source = ?," +
                "visibility = ?, description = ?, property_type = ?, datatype = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "meta_property", "id");
            property.setId(id);

            ps.setLong(1, id);
            ps.setString(2, property.getName());
            ps.setString(3, property.getUri());
            ps.setString(4, property.getSource());
            ps.setInt(5, 0);
            ps.setString(6, property.getDescription());
            ps.setString(7, property.getPropertyType());
            ps.setString(8, property.getDatatype());

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

    public Collection<Entity> findAllEntities() {
        String sql = "select * from entity e left join property pr on pr.entity_id = e.id " +
                " left join meta_property mp on pr.uri = mp.uri  and mp.visibility = 1 " +
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
                MetaProperty mp = new MetaProperty(rs);
                property.setMetaProperty(mp);

                Integer show = rs.getInt("mp.visibility");
                property.setVisibility(show);

                if (property.getVisibility().equals(3))
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

    public Entity findEntityById(Long id) {
        String sql = "select * from entity e left join property pr on pr.entity_id = e.id " +
                " left join meta_property mp on pr.uri = mp.uri   " + // and mp.visibility != 0
                " left join category c on e.category_id = c.id   " + // and mp.visibility != 0
                " where e.id = ?";

        Connection conn = null;

        Entity entity = null;
        try {
            conn = kbDataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                entity = new Entity(rs);
                String categoryName = rs.getString("c.name");
                entity.setCategoryName(categoryName);

                Long categoryId = rs.getLong("c.id");
                entity.setCategoryId(categoryId);
            }

            while (rs.next()) {
                Property property = new Property(rs);
                MetaProperty mp = new MetaProperty(rs);
                property.setMetaProperty(mp);

                Integer show = rs.getInt("mp.visibility");
                property.setVisibility(show);


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

    public Collection<Entity> findAllEntitiesLazy() {
        String sql = "select * from entity e " +
                " where entity_type <> 'web-page-annotation' &&  entity_type <> 'non-semantic-web';";

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

    public List<Property> findAllProperties() {
        String sql = "select * from property pr;";

        Connection conn = null;

        List<Property> properties = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                properties.add(new Property(rs));
            }

            rs.close();
            ps.close();

            return properties;
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


    public List<Property> findPropertiesByEntity(Long id) {
        String sql = "select * from property pr left join meta_property mp on mp.id = pr.meta_property_id where pr.id = >;";

        Connection conn = null;

        List<Property> properties = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Integer show = rs.getInt("visibility");
                Property p = new Property(rs);
                MetaProperty mp = new MetaProperty(rs);
                p.setMetaProperty(mp);
                p.setVisibility(show);

                properties.add(p);
            }

            rs.close();
            ps.close();

            return properties;
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

    public Collection<MetaProperty> findAllMetaproperties() {
        String sql = "select * from meta_property mp;";

        Connection conn = null;

        List<MetaProperty> metaProperties = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                metaProperties.add(new MetaProperty(rs));
            }

            rs.close();
            ps.close();

            return metaProperties;
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

    public MetaProperty findMetapropertyByUri(String uri) {
        String sql = "select * from meta_property mp where mp.uri = ?;";

        Connection conn = null;

        MetaProperty metaProperty = null;
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, uri);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                metaProperty = new MetaProperty(rs);
            }

            rs.close();
            ps.close();

            return metaProperty;
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

    public Collection<Entity> findAnnotationEntities(Long webPageEntityId) {
        String sql = "select * from entity e " +
                " left join property pr on pr.entity_id = e.id " +
                " left join meta_property mp on pr.meta_property_id = mp.id and mp.visibility = 1 " +
                " where e.web_page_entity_id = ?;";

        Connection conn = null;

        Entity entity = null;
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, webPageEntityId);

            Map<Long, Entity> map = new HashMap<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entity = new Entity(rs);

                Property property = new Property(rs);
                MetaProperty mp = new MetaProperty(rs);
                property.setMetaProperty(mp);

                if (property.getVisibility() != null && property.getVisibility().equals(3)) {
                    if (map.get(entity.getId()) != null) {

                        map.get(entity.getId()).getProperties().add(property);
                    } else {
                        entity.getProperties().add(property);
                    }
                }

                map.put(entity.getId(), entity);
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

}
