package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mysql.cj.xdevapi.PreparableStatement;
import itstep.learning.dal.dto.shop.Category;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class CategoryDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public CategoryDao(DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public Category create(Category category) {
        if (category == null) {
            return null;
        }
        category.setId(UUID.randomUUID());
        String sql = "INSERT INTO `categories`" +
                "(`category_id`, `category_name`, `category_description`, `category_img_url`, `category_delete_dt` )" +
                "VALUES (?, ?, ?, ?, ?)";

        try(PreparedStatement prep = dbService.getConnection().prepareStatement(sql))
        {
            prep.setString(1, category.getId().toString());
            prep.setString(2, category.getName());
            prep.setString(3, category.getDescription());
            prep.setString(4, category.getImageUrl());
            if( category. getDeleteDt() != null ) {
                prep.setTimestamp( 5, new Timestamp( category. getDeleteDt().getTime() ) );
            }
            else {
                prep.setTimestamp(5, null);
            }
            prep.executeUpdate();
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + "--" + sql);
            return null;
        }
        return category;
    }

    public List<Category> read() {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT * FROM categories";
        try(Statement stmt = dbService.getConnection().createStatement() ) {
            ResultSet rs = stmt.executeQuery( sql );
            while ( rs.next() ) {
                categories.add( new Category( rs ) ) ;
            }
            rs.close();
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
        }

        return categories;
    }

}
