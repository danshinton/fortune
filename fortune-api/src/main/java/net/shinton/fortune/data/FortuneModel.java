package net.shinton.fortune.data;

import java.util.List;
import net.shinton.exception.DuplicateEntryException;
import net.shinton.fortune.data.accessor.FortuneAccessor;
import org.jdbi.v3.core.Jdbi;

/**
 * This class provides an easy-to-use interface for accessing the data in
 * <code>fortune</code> table in the database.
 */
public class FortuneModel {
  private final Jdbi jdbi;

  /**
   * Construct a new model using the supplied database connection.
   *
   * @param jdbi The database connection
   */
  public FortuneModel(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  /**
   * Retrieves all fortunes from the database.
   *
   * @return A list of all fortunes
   */
  public List<String> getAllFortunes() {
    return jdbi.withHandle(handle -> new FortuneAccessor(handle).getAllFortunes());
  }

  /**
   * Get a single random fortune from the database
   *
   * @return A fortune
   */
  public String getRandomFortune() {
    return jdbi.withHandle(handle -> new FortuneAccessor(handle).getRandomFortune());
  }

  /**
   * Adds a fortune to the database
   *
   * @param fortune The fortune to add
   * @return <code>true</code> if added
   * @throws DuplicateEntryException when the fortune already exists in the database
   */
  public boolean addFortune(String fortune) throws DuplicateEntryException {
    return jdbi.withHandle(handle -> new FortuneAccessor(handle).addFortune(fortune));
  }
}
