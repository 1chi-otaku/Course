package itstep.learning.services.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public interface DbService {
    Connection getConnection() throws SQLException;  // Повертаємо Connection
}