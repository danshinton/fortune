package net.shinton.fortune.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Paths;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

/**
 * Factory class to create a new connection to the database. If the database does
 * not already exist, it will create it. If the database needs to be upgraded, it
 * will do so.
 */
public class FortuneDatabaseFactory {

  /**
   * Create a new connection to the database.
   *
   * @param jdbcUrl The url to the database
   * @return The database connection
   */
  public Jdbi newDatabase(String jdbcUrl) {
    // Create database pool
    HikariConfig cfg = new HikariConfig();
    cfg.setJdbcUrl(jdbcUrl);

    DataSource dataSource = new HikariDataSource(cfg);

    // Make sure the database is up-to-date
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations(Paths.get("classpath:db/migration").toString())
        .load();

    flyway.migrate();

    // Return the database object
    return Jdbi.create(dataSource);
  }
}
