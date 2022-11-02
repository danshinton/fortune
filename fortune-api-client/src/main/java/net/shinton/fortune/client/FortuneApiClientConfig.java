package net.shinton.fortune.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;
import org.aeonbits.owner.Config;
import org.immutables.value.Value;

/**
 * <p>
 *   This is an interface that is written to be used in two ways.
 * </p>
 *
 * <p>
 *   The first way that it can be used is with the <code>Owner</code> library to
 *   read configuration from the environment. For example:
 * </p>
 * <pre>
 * ConfigFactory.create(FortuneApiClientConfig.class, System.getenv());
 * </pre>
 * <p>
 *   The second way uses the <code>Immutables</code> framework to manually build
 *   configuration objects. For example:
 * </p>
 * <pre>
 * FortuneApiClientConfig config = ImmutableFortuneApiClientConfig.builder()
 *     .logLevel("ERROR")
 *     .build();
 * </pre>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFortuneApiClientConfig.class)
@JsonDeserialize(as = ImmutableFortuneApiClientConfig.class)
public interface FortuneApiClientConfig extends Config {
  /**
   * A <code>serialVersionUID</code> is not strictly needed on an interface, but
   * in this case, it is required because the value carries over into the
   * generated immutable class.
   */
  long serialVersionUID = -2492063113673429029L;

  /**
   * The base URL of the Fortune API
   *
   * @return The base URL
   */
  @Key("FORTUNE_URL")
  String baseUrl();

  /**
   * The number of milliseconds that we should wait when trying to connect to
   * the API.
   *
   * @return The timeout in ms
   */
  @Key("FORTUNE_CONNECT_TIMEOUT")
  @DefaultValue("10000")
  Long connectTimeout();

  /**
   * The number of milliseconds that we should wait when trying to read the
   * response from the API.
   *
   * @return The timeout in ms
   */
  @Key("FORTUNE_READ_TIMEOUT")
  @DefaultValue("10000")
  Long readTimeout();

  /**
   * The number of milliseconds that we should wait when trying to write to
   * the API.
   *
   * @return The timeout in ms
   */
  @Key("FORTUNE_WRITE_TIMEOUT")
  @DefaultValue("10000")
  Long writeTimeout();

  /**
   * A Base64 encoded PEM file of the certificate chain used to establish an SSL
   * connection. This is typically only needed for self-signed certificates.
   *
   * @return The timeout in ms
   */
  @Nullable
  @Key("FORTUNE_SSL_CERTS")
  String sslCerts();
}
