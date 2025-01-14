package itstep.learning.dal.dto.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Review {
    private UUID id;
    private UUID productId;
    private String name;
    private String message;

    public Review() {
    }

    //инициализирует объект на основе строки данных из SQLзапроса:
    public Review(ResultSet rs) throws SQLException {
        this.setId(UUID.fromString(rs.getString("review_id")));
        this.setProductId(UUID.fromString(rs.getString("product_id")));
        this.setName(rs.getString("review_name"));
        this.setMessage(rs.getString("review_message"));
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
