package net.shinton.util;

import java.util.Arrays;
import org.tinylog.Level;
import org.tinylog.core.TinylogLoggingProvider;
import org.tinylog.format.MessageFormatter;

/**
 * Starting with <code>tinylog v2</code>, configurations are immutable. This
 * means no switching the log level at runtime. So this custom provider is
 * needed to accomplish the same task. For it to work, set all writers to the
 * <code>TRACE</code> level and add <code>provider = mutable</code> to the
 * <code>tinylog.properties</code>.
 */
public class MutableLoggingProvider extends TinylogLoggingProvider {
  private static volatile Level activeLevel = Level.INFO;

  /**
   * Sets the level of the logging framework.
   *
   * @param level The level
   * @return <code>true</code> if the level was set
   */
  public static boolean setLevel(Level level) {
    if (level != null) {
      activeLevel = level;
      return true;
    }
    return false;
  }

  /**
   * <p>
   *   Sets the level of the logging framework. The level can be one of the
   *   following:
   * </p>
   * <ul>
   *   <li>TRACE</li>
   *   <li>DEBUG</li>
   *   <li>INFO</li>
   *   <li>WARN</li>
   *   <li>ERROR</li>
   *   <li>OFF</li>
   * </ul>
   *
   * @param level The level as a string
   * @return <code>true</code> if the level was set
   */
  public static boolean setLevel(String level) {
    return setLevel(getLevel(level));
  }

  /**
   * Gets the current logging level.
   *
   * @return the level
   */
  public static Level getLevel() {
    return activeLevel;
  }

  /**
   * Utility method to convert a string level to a {@link Level}.
   *
   * @param level The level
   * @return The converted level
   */
  private static Level getLevel(String level) {
    return Arrays.stream(Level.values())
        .filter(l -> l.name().equalsIgnoreCase(level))
        .findAny()
        .orElse(null);
  }

  /**
   * Checks whether log entries with given tag and severity level will be output.
   *
   * @param depth Depth of caller in stack trace (e.g. '1' if there is only one
   *              method between caller and this method in the stack trace)
   * @param tag Tag of log entry or null if untagged
   * @param level Severity level of log entry
   * @return <code>true</code> if given severity level is enabled, false if disabled
   */
  @Override
  public boolean isEnabled(int depth, String tag, Level level) {
    return isEnabled(level) && super.isEnabled(depth + 1, tag, level);
  }

  /**
   * Utility method to quickly check to see if a particular log level is enabled.
   *
   * @param level The level to check
   * @return <code>true</code> if enabled
   */
  private boolean isEnabled(Level level) {
    return (activeLevel.ordinal() <= level.ordinal());
  }

  /**
   * Provides a regular log entry.
   *
   * @param depth Depth of caller in stack trace (e.g. '1' if there is only one
   *              method between caller and this method in the stack trace)
   * @param tag Tag of log entry or null if untagged
   * @param level Severity level of log entry
   * @param exception Exception to log or null
   * @param formatter Formatter for text message, only required if there are any
   *                  arguments to insert
   * @param obj Message to log or null
   * @param arguments  Arguments for message or null
   */
  @Override
  public void log(int depth, String tag, Level level, Throwable exception, MessageFormatter formatter,
                  Object obj, Object... arguments) {

    if (isEnabled(level)) {
      super.log(depth + 1, tag, level, exception, formatter, obj, arguments);
    }
  }

  /**
   * Provides a regular log entry.
   *
   * @param loggerClassName Fully-qualified class name of the logger instance
   * @param tag Tag of log entry or null if untagged
   * @param level Severity level of log entry
   * @param exception Exception to log or null
   * @param formatter Formatter for text message, only required if there are any
   *                  arguments to insert
   * @param obj Message to log or null
   * @param arguments Arguments for message or null
   */
  @Override
  public void log(String loggerClassName, String tag, Level level, Throwable exception,
                  MessageFormatter formatter, Object obj, Object... arguments) {

    if ((loggerClassName.startsWith("net.shinton")) || isEnabled(level)) {
      super.log(loggerClassName, tag, level, exception, formatter, obj, arguments);
    }
  }
}
