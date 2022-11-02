package net.shinton.fortune.client.factory;

import net.shinton.fortune.client.FortuneApiClientConfig;
import org.aeonbits.owner.ConfigFactory;

/**
 * Factory class to provide configuration to the application without having to
 * reparse the environment every time it is needed
 */
public class FortuneApiClientConfigFactory {
  private static volatile FortuneApiClientConfig config = null;

  /**
   * If this is the first time called, it will parse the environment and create
   * a new <code>FortuneApiClientConfig</code>. Each subsequent call will receive
   * that same object.
   *
   * @return The configuration
   */
  public FortuneApiClientConfig getConfig() {
    if (config == null) {
      synchronized (FortuneApiClientConfigFactory.class) {
        config = ConfigFactory.create(FortuneApiClientConfig.class, System.getenv());
      }
    }

    return config;
  }
}