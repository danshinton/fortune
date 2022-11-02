package net.shinton.fortune.factory;

import net.shinton.fortune.FortuneApiConfig;
import org.aeonbits.owner.ConfigFactory;

/**
 * Factory class to provide configuration to the application without having to
 * reparse the environment every time it is needed
 */
public class FortuneApiConfigFactory {
  private static volatile FortuneApiConfig config = null;

  /**
   * If this is the first time called, it will parse the environment and create
   * a new <code>FortuneApiConfig</code>. Each subsequent call will receive that
   * same object.
   *
   * @return The configuration
   */
  public FortuneApiConfig getConfig() {
    if (config == null) {
      synchronized (FortuneApiConfigFactory.class) {
        config = ConfigFactory.create(FortuneApiConfig.class, System.getenv());
      }
    }

    return config;
  }
}
