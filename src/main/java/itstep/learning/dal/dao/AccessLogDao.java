package itstep.learning.dal.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.db.DbService;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Logger;

@Singleton
public class AccessLogDao {
    private final DbService dbService;
    private final Logger logger;

    @Inject
    public AccessLogDao(DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }

    public boolean install() {
        String sql = "CREATE TABLE IF NOT EXISTS `access_log` (" +
                " `log_id` CHAR(36) PRIMARY KEY DEFAULT (UUID())," +
                " `user_id` CHAR(36) NOT NULL," +
                " `page_url` VARCHAR(255) NOT NULL," +
                " `access_dt` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Statement stmt = dbService.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
            return false;
        }

        return true;
    }

    // Метод для додавання запису у журнал доступу
    public boolean logAccess(String userId, String pageUrl) {
        String sql = "INSERT INTO `access_log` (`user_id`, `page_url`) VALUES (?, ?)";

        try (PreparedStatement prep = dbService.getConnection().prepareStatement(sql)) {
            prep.setString(1, userId);
            prep.setString(2, pageUrl);
            prep.executeUpdate();
        } catch (SQLException ex) {
            logger.warning(ex.getMessage() + " -- " + sql);
            return false;
        }

        return true;
    }
}
