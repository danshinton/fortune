package net.shinton.fortune.data.accessor;

import java.util.List;
import net.shinton.exception.DuplicateEntryException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

/**
 * This class contains the specific logic for querying the <code>fortune</code>
 * table in the database to supply data to the model.
 */
public class FortuneAccessor {
  private final Handle handle;

  private static final String GET_ALL =
      "SELECT quote FROM fortune";

  private static final String GET_RANDOM_FORTUNE =
      "SELECT quote" +
      "  FROM fortune " +
      " ORDER BY RANDOM() " +
      " LIMIT 1";

  private static final String INSERT_FORTUNE =
      "INSERT INTO fortune ('quote') VALUES (:fortune)";

  /**
   * Create a new accessor
   *
   * @param handle The database connection to use for queries
   */
  public FortuneAccessor(final Handle handle) {
    this.handle = handle;
  }

  /**
   * Get all the fortunes from the database
   *
   * @return A list of all fortunes
   */
  public List<String> getAllFortunes() {
    return handle.createQuery(GET_ALL)
        .mapTo(String.class)
        .list();
  }

  /**
   * Gets a random fortune from the database
   *
   * @return The random fortune
   */
  public String getRandomFortune() {
    return handle.createQuery(GET_RANDOM_FORTUNE)
        .mapTo(String.class)
        .first();
  }

  /**
   * Adds a fortune to the database
   *
   * @param fortune The fortune to add
   * @return <code>true</code> if successful
   * @throws DuplicateEntryException The fortune already exists in the database
   */
  public boolean addFortune(String fortune) throws DuplicateEntryException {
    try {
      int updated = handle.createUpdate(INSERT_FORTUNE)
          .bind("fortune", fortune)
          .execute();

      return (updated == 1);

    } catch (UnableToExecuteStatementException e) {
      // Not pretty, but it is what it is
      if (e.getMessage().contains("SQLITE_CONSTRAINT_UNIQUE")) {
        throw new DuplicateEntryException();
      }

      throw e;
    }
  }
}
