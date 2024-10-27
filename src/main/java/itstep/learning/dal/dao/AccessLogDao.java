package itstep.learning.dal.dao;

import com.google.inject.Inject;
import itstep.learning.services.db.DbService;

import java.lang.annotation.Inherited;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class AccessLogDao {
    private final DbService dbService;
    private  final Logger logger;

    @Inject
    public AccessLogDao(DbService dbService, Logger logger) {
        this.dbService = dbService;
        this.logger = logger;
    }


    public  boolean install(){
        String sql = "CREATE TABLE  IF NOT EXISTS `access_log` (" +
                "`access_log_id`  CHAR(36)     PRIMARY KEY  DEFAULT( UUID() )," +
                "`user_id`        CHAR(36)     NOT NULL," +
                "`timestamp`      DATETIME     DEFAULT CURRENT_TIMESTAMP," +
                "`page_visited`   VARCHAR(32)  NOT NULL" +
                ") ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }

        sql = "INSERT INTO `access_log` (`user_id`, `page_visited`) VALUES ('7dd7d8a9-815e-11ef-bb48-fcfbf6dd7098', 'main' )";

        try( Statement stmt = dbService.getConnection().createStatement() ) {
            stmt.executeUpdate( sql ) ;
        }
        catch( SQLException ex ) {
            logger.warning( ex.getMessage() + " -- " + sql );
            return false;
        }



        return true;
    }

}


/*
* |Access Log|
* |access_log_id
* |user_id
* |timestamp
* |pageVisited
 *
* */
