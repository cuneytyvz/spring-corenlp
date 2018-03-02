package com.gsu.knowledgebase.repository;

import com.gsu.common.util.DateUtils;
import com.gsu.common.util.MaxIdCalculator;
import com.gsu.knowledgebase.model.*;
import com.gsu.knowledgebase.util.Constants;
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
                " wikipedia_uri = ?,short_description = ?, small_image = ?, note = ?, subcategory_id = ?, source = ?," +
                " secondary_image = ?, small_secondary_image = ?, web_page_text = ?";

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
            ps.setLong(16, entity.getSubCategoryId());
            ps.setString(17, entity.getSource());
            ps.setString(18, entity.getSecondaryImage());
            ps.setString(19, entity.getSmallSecondaryImage());
            ps.setString(20, entity.getWebPageText());
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

    public Long saveAnnotationItems(List<AnnotationItem> items) throws Exception {

        String sql = "insert into annotation_item set id = ?, surface_form = ?, types = ?, offset = ?, uri = ?" +
                ", entity_id = ?, referenced_entity_id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "annotation_item", "id");

            for (AnnotationItem item : items) {
                ps.setLong(1, id++);
                ps.setString(2, item.getSurfaceForm());
                ps.setString(3, item.getTypes());
                ps.setInt(4, item.getOffset());
                ps.setString(5, item.getUri());

                if (item.getEntityId() == null) {
                    ps.setNull(6, Types.BIGINT);
                } else {
                    ps.setLong(6, item.getEntityId());
                }

                ps.setLong(7, item.getReferencedEntityId());

                ps.execute();
            }

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

    public List<AnnotationItem> findAnnotationItems(Integer entityId) throws Exception {

        String sql = "select * from annotation_item ai left join entity e on e.id = ai.referenced_entity_id where referenced_entity_id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, entityId);

            List<AnnotationItem> items = new ArrayList<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AnnotationItem item = new AnnotationItem(rs);
                if (rs.getLong("e.id") != 0) {
                    item.setReferencedEntity(new Entity(rs));
                }

                items.add(item);
            }

            ps.close();

            return items;
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

    public Long removeEntity(Long id) throws Exception {

        String sql = "delete from entity where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setLong(1, id);

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
                " uri = ?, datatype = ?, source = ?, entity_id = ?, value_label = ?, property_type = ?, meta_property_id = ?";

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
            ps.setLong(12, property.getMetaPropertyId());

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

                try {
                    ps.execute();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    public void addEntityToCategory(Long entityId, Long categoryId) {

        String sql = "update entity set category_id = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, categoryId);
            ps.setLong(2, entityId);

            ps.execute();
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

    public void addEntityToSubCategory(Long entityId, Long subCategoryId) {

        String sql = "update entity set subcategory_id = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, subCategoryId);
            ps.setLong(2, entityId);

            ps.execute();
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

    public void removeEntityFromCategory(Long entityId) {

        String sql = "update entity set category_id = ?, subcategory_id = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, 1);
            ps.setLong(2, entityId);
            ps.setNull(3, Types.BIGINT);

            ps.execute();
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

    public void removeEntityFromSubCategory(Long entityId) {

        String sql = "update entity set subcategory_id = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setNull(1, Types.BIGINT);
            ps.setLong(2, entityId);

            ps.execute();
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

                try {
                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    public Long saveMetaProperty(MetaProperty property) {

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
                " left join meta_property mp on pr.meta_property_id = mp.id " + // pr.uri = mp.uri
                " left join category c on e.category_id = c.id   " + // and mp.visibility != 0
                " where e.id = ?";

        Connection conn = null;

        Entity entity = null;
        try {
            conn = kbDataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (entity == null) {
                    entity = new Entity(rs);
                    String categoryName = rs.getString("c.name");
                    entity.setCategoryName(categoryName);

                    Long categoryId = rs.getLong("c.id");
                    entity.setCategoryId(categoryId);
                }

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
                " where entity_type <> 'web-page-annotation' " +
                " and  entity_type <> 'non-semantic-web' " +
                " and (source = 'memory-item' or source = 'custom-memory-item');";

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

    public Collection<Entity> findAllMainPageEntitiesLazy() {
        String sql = "select * from entity e " +
                " where entity_type <> 'custom' or entity_type = 'semantic-web' or " +
                " and (source = 'memory-item' or source = 'custom-memory-item');";

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

    public Collection<MetaProperty> findPropertiesByPrefix(String prefix) {
        String sql = "select * from meta_property mp where LOWER(mp.name) like ? or LOWER(mp.proper_name) like ?";

        Connection conn = null;

        HashMap<Long, Entity> map = new HashMap<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, prefix.toLowerCase() + '%');
            ps.setString(2, prefix.toLowerCase() + '%');

            ResultSet rs = ps.executeQuery();

            List<MetaProperty> metaProperties = new ArrayList<>();
            while (rs.next()) {
                MetaProperty mp = new MetaProperty(rs);

                metaProperties.add(mp);
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


    public List<Property> findPropertiesByTriples(Long subjectEntityId, Long predicatePropertyId, String objectValue) {
        String sql = "select * from property pr left join meta_property mp on mp.id = pr.meta_property_id " +
                " where pr.entity_id = ? and meta_property_id = ? and value_label = ?;";

        Connection conn = null;

        List<Property> properties = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, subjectEntityId);
            ps.setLong(2, predicatePropertyId);
            ps.setString(3, objectValue);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Property p = new Property(rs);
                MetaProperty mp = new MetaProperty(rs);
                p.setMetaProperty(mp);

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

    public User findUserByUsername(String username) throws Exception {

        String sql = "select * from user u where LOWER(u.username) = LOWER(?);";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            User user = null;
            if (rs.next())
                user = new User(rs);

            ps.close();

            return user;
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

    public User findUserById(Long id) throws Exception {

        String sql = "select * from user u where u.id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            User user = null;
            if (rs.next())
                user = new User(rs);

            ps.close();

            return user;
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

    public User findUserByEmail(String email) {

        String sql = "select * from user u where LOWER(u.email) = LOWER(?);";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            User user = null;
            if (rs.next())
                user = new User(rs);

            ps.close();

            return user;
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

    public Long saveLogin(Login login) {

        String sql = "insert into user set id = ?, user_id = ?, cr_date = ?, type = ?, success = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "login", "id");

            ps.setLong(1, id);
            ps.setLong(2, login.getId());
            ps.setTimestamp(3, new Timestamp(login.getCrDate().getMillis()));
            ps.setInt(4,login.getType());
            ps.setBoolean(5,login.isSuccess());

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

    public Long saveUser(User user) {

        String sql = "insert into user set id = ?, username = ?, password= ?, email = LOWER(?), first_name = ?," +
                " last_name = ?, role_id = ?, status = ?, login_try_count = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "user", "id");

            ps.setLong(1, id);
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getFirstName());
            ps.setString(6, user.getLastName());
            ps.setLong(7, user.getRoleId());
            ps.setInt(8, user.getStatus());
            ps.setInt(9, 0);

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

    public void updateUser(User user) {

        String sql = "update user set username = ?, password= ?, email = LOWER(?), first_name = ?," +
                " last_name = ?, role_id = ?, status = ? where id = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getLastName());
            ps.setLong(6, user.getRoleId());
            ps.setInt(7, user.getStatus());
            ps.setLong(8, user.getId());

            ps.execute();

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

    public void updateUserStatus(Long userId, Integer status) {

        String sql = "update user set status = ? where id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setLong(1, status);
            ps.setLong(2, userId);

            ps.execute();

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

    public void updateUserLoginTryCount(Long userId, Integer count) {

        String sql = "update user set login_try_count = ? where id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);


            ps.setLong(1, count);
            ps.setLong(2, userId);

            ps.execute();

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

    public Long saveUserConfirmation(UserConfirmation userConfirmation) {

        String sql = "insert into user_confirmation set id = ?, user_id = ?, confirmation_code = ?, status = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "user_confirmation", "id");

            ps.setLong(1, id);
            ps.setLong(2, userConfirmation.getUserId());
            ps.setString(3, userConfirmation.getConfirmationCode());
            ps.setInt(4, userConfirmation.getStatus());

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

    public void updateUserConfirmationStatus(Long id, Integer status) {
        String sql = "update user_confirmation set  status = ? where id = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, status);
            ps.setLong(2, id);

            ps.execute();

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

    public void deleteUserConfirmation(UserConfirmation userConfirmation) {

        String sql = "delete from user_confirmation where confirmation_code = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, userConfirmation.getConfirmationCode());

            ps.execute();
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

    public UserConfirmation findAwaitingUserConfirmationByCode(String confirmationCode) throws Exception {

        String sql = "select * from user_confirmation uc where confirmation_code = ? and status = ?;";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, confirmationCode);
            ps.setInt(2, Constants.USER_CONFIRMATION_STATUS_AWAITING);

            ResultSet rs = ps.executeQuery();

            UserConfirmation uc = null;
            if (rs.next()) {
                uc = new UserConfirmation(rs);
            }

            ps.close();

            return uc;
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

    public Long saveSubcategory(Long categoryId, String name) {

        String sql = "insert into subcategory set id = ?, category_id = ?, name = ?";

        Connection conn = null;

        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            Long id = maxIdCalculator.getMaxIdFromTable(conn, true, "subcategory", "id");

            ps.setLong(1, id);
            ps.setLong(2, categoryId);
            ps.setString(3, name);

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
        String sql = "select * from category c left join subcategory sc on c.id = sc.category_id;";

        Connection conn = null;

        Map<Long, Category> map = new HashMap<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Long id = rs.getLong("c.id");

                SubCategory sc = new SubCategory(rs);
                if (map.containsKey(id)) {
                    if (sc.getId() != 0)
                        map.get(id).addSubCategory(sc);
                } else {
                    Category c = new Category(rs);
                    if (sc.getId() != 0)
                        c.addSubCategory(sc);

                    map.put(id, c);
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

    public Collection<SubCategory> findSubCategories(Long categoryId) {
        String sql = "select * from subcategory sc where sc.category_id = ?;";

        Connection conn = null;

        List<SubCategory> categories = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, categoryId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                categories.add(new SubCategory(rs));
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

    public Collection<SubCategory> findAllSubCategories() {
        String sql = "select * from subcategory sc;";

        Connection conn = null;

        List<SubCategory> categories = new ArrayList<>();
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                categories.add(new SubCategory(rs));
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

    public Entity findEntityByDbpediaUriWikidataId(Entity e) {
        String sql = "select * from entity e left join property pr on pr.entity_id = e.id where ";

        int counter = 0;
        if (e.getDbpediaUri() != null && e.getDbpediaUri().length() > 0) {
            sql += " dbpedia_uri = ? ";
            counter++;
        }
        if (e.getWikidataId() != null && e.getWikidataId().length() > 0) {
            if (counter > 0)
                sql += " or ";

            sql += " wikidata_id = ?";
            counter++;
        }

        Connection conn = null;

        Entity entity = null;
        try {
            conn = kbDataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            counter = 1;
            if (e.getDbpediaUri() != null && e.getDbpediaUri().length() > 0) {
                ps.setString(counter++, e.getDbpediaUri());
            }

            if (e.getWikidataId() != null && e.getWikidataId().length() > 0) {
                ps.setString(counter++, e.getWikidataId());
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entity = new Entity(rs);

                Property property = new Property(rs);
                entity.getProperties().add(property);
            }

            rs.close();
            ps.close();

            return entity;
        } catch (SQLException e1) {
            throw new RuntimeException(e1);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e2) {
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
