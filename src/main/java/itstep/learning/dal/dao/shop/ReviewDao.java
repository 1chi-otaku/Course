package itstep.learning.dal.dao.shop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dto.shop.Review;
import itstep.learning.services.db.DbService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class ReviewDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public ReviewDao(DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    // Create a review
    public Review create(Review review) {
        if (review == null) {
            return null;
        }
        review.setId(UUID.randomUUID());
        String sql = "INSERT INTO `reviews`" +
                "(`review_id`, `product_id`, `review_name`, `review_message`)" +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, review.getId().toString());
            prep.setString(2, review.getProductId().toString());
            prep.setString(3, review.getName());
            prep.setString(4, review.getMessage());
            prep.executeUpdate();
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
            return null;
        }
        return review;
    }

    // Get review by ID
    public Review getById(UUID id) {
        Review review = null;
        String sql = "SELECT * FROM `reviews` WHERE `review_id` = ?";

        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, id.toString());
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                review = new Review(rs);
            }
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
        }

        return review;
    }

    public List<Review> getReviewsByProductId(String productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE product_id = ?";

        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, productId);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                reviews.add(new Review(rs)); // предполагается, что есть конструктор в Review, который принимает ResultSet
            }
            rs.close();
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
        }

        return reviews;
    }



    // Install the reviews table (if not exists)
    public boolean install() {
        String sql = "CREATE TABLE IF NOT EXISTS `reviews` (" +
                " `review_id`        CHAR(36)        PRIMARY KEY DEFAULT (UUID())," +
                " `product_id`       CHAR(36)        NOT NULL," +
                " `review_name`      VARCHAR(64)     NOT NULL," +
                " `review_message`   TEXT            NOT NULL," +
                " FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
            return false;
        }

        return true;
    }
}
