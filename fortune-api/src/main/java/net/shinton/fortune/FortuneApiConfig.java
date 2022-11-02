package net.shinton.fortune;

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
 * ConfigFactory.create(FortuneApiConfig.class, System.getenv());
 * </pre>
 * <p>
 *   The second way uses the <code>Immutables</code> framework to manually build
 *   configuration objects. For example:
 * </p>
 * <pre>
 * FortuneApiConfig config = ImmutableFortuneApiConfig.builder()
 *     .logLevel("ERROR")
 *     .build();
 * </pre>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableFortuneApiConfig.class)
@JsonDeserialize(as = ImmutableFortuneApiConfig.class)
public interface FortuneApiConfig extends Config {

  /**
   * A <code>serialVersionUID</code> is not strcitly needed on an interface, but
   * in this case, it is required because the value carries over into the
   * generated immutable class.
   */
  long serialVersionUID = -5939512451676900874L;

  /**
   * The log level to initialize the service.
   *
   * @return The log level
   */
  @Key("FORTUNE_LOG_LEVEL")
  @DefaultValue("INFO")
  String logLevel();

  /**
   * The JDBC connect string used to connect to the database containing fortunes.
   *
   * @return The JDBC connect string
   */
  @Key("FORTUNE_JDBC_URL")
  @DefaultValue("jdbc:sqlite:/fortune-data/fortune-api.db")
  String jdbcUrl();

  /**
   * The port to listen for HTTP connections.
   *
   * @return The HTTP port
   */
  @Key("FORTUNE_HTTP_PORT")
  @DefaultValue("80")
  int port();

  /**
   * The port to listen for HTTPS connections.
   *
   * @return The HTTPS port
   */
  @Key("FORTUNE_HTTPS_PORT")
  @DefaultValue("443")
  int sslPort();

  /**
   * A Base64 encoded RSA private signing key for the SSL certificate
   *
   * @return The private key
   */
  @Nullable
  @Key("FORTUNE_SSL_KEY")
  String sslKey();

  /**
   * A Base64 encoded PEM of the SSL certificate chain.
   *
   * @return The certificate chain
   */
  @Nullable
  @Key("FORTUNE_SSL_CERTS")
  String sslCerts();

  /**
   * A Base64 JWT signing key.
   *
   * @return The signing key
   */
  @Key("FORTUNE_JWT_SIGNING_KEY")
  String jwtSigningKey();

  /**
   * The publicly facing host name used to validate auth tokens
   *
   * @return The host name
   */
  @Key("FORTUNE_PUBLIC_HOST")
  String publicHost();
}