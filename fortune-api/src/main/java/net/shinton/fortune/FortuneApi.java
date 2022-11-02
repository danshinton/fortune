package net.shinton.fortune;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.HttpStatus;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.shinton.fortune.factory.FortuneApiConfigFactory;
import net.shinton.fortune.factory.FortuneDatabaseFactory;
import net.shinton.fortune.factory.KeystoreFactory;
import net.shinton.fortune.handler.ErrorHandler;
import net.shinton.fortune.handler.FortuneGetAllHandler;
import net.shinton.fortune.handler.FortuneGetHandler;
import net.shinton.fortune.handler.FortunePostHandler;
import net.shinton.util.MutableLoggingProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinylog.Level;

/**
 * The main class for the Fortune API microservice. When running, it provides
 * RESTful services to access and mutate fortune data.
 */
public class FortuneApi implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(FortuneApi.class);
  private final FortuneApiConfig config;
  private Jdbi jdbi;
  private Javalin app;

  /**
   * Create a new Fortune API using the config from the environment
   */
  public FortuneApi() {
    this(new FortuneApiConfigFactory().getConfig());
  }

  /**
   * Create a new Fortune API using supplied config
   *
   * @param config The configuration to use
   */
  public FortuneApi(FortuneApiConfig config) {
    this.config = config;
  }

  /**
   * Starts the Fortune API web service. To stop the service, call the
   * {@link #close()} method.
   */
  public void run() {
    // Set the log level
    MutableLoggingProvider.setLevel(config.logLevel());

    // Initialize the database
    jdbi = new FortuneDatabaseFactory().newDatabase(config.jdbcUrl());

    // Create and configure the Javalin server
    ErrorHandler errorHandler = new ErrorHandler(config);

    app = Javalin.create(this::configure)
        .get("/api/v1/fortune", new FortuneGetHandler(config, jdbi))
        .post("/api/v1/fortune", new FortunePostHandler(config, jdbi))
        .get("/api/v1/fortune/all", new FortuneGetAllHandler(config, jdbi))
        .error(HttpStatus.NOT_FOUND, errorHandler)
        .error(HttpStatus.INTERNAL_SERVER_ERROR, errorHandler)
        .start();
  }

  /**
   * Configuration method for Javalin. This method handles installing SSL certs,
   * port configuration, and timeout settings.
   *
   * @param javalinConfig The Javalin configuration to initialize
   */
  private void configure(JavalinConfig javalinConfig) {
    KeyStore keystore = null;

    /*
     * Try to create a keystore based on the configuration
     */
    try {
      keystore = new KeystoreFactory().newServerKeystore(config.sslKey(), config.sslCerts());
    } catch (Exception e) {
      log.error("Could not create keystore", e);
    }
    List<Connector> connectors = new ArrayList<>();
    Server server = new Server();

    /*
     * If we were able to create a keystore, then configure Javalin to accept
     * HTTPS requests. Otherwise, accept HTTP requests.
     */
    if (keystore != null) {
      SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
      sslContextFactory.setKeyStore(keystore);

      ServerConnector sslConnector = new ServerConnector(server, sslContextFactory);
      sslConnector.setPort(config.sslPort());
      connectors.add(sslConnector);

    } else {
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(config.port());
      connectors.add(connector);
    }

    server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

    /*
     * Finalize the config and turn off the banner for the sake of logging
     */
    javalinConfig.jetty.server(() -> server);
    javalinConfig.showJavalinBanner = false;
  }

  /**
   * Determines the local URLs that the FortuneApi is listening on.
   *
   * @return The list of URLs
   */
  public List<String> getLocalAddresses() {
    if (app == null) {
      return null;
    }

    /*
     * This looks very convoluted, but it is actually how Javalin does it looking
     * at the source code for the log messages it prints when it starts up.
     */
    return Arrays.stream(app.jettyServer().server().getConnectors())
        .filter(c -> c instanceof ServerConnector)
        .map(c -> {
          ServerConnector serverConnector = (ServerConnector) c;
          return String.format(Locale.ROOT, "%s://%s:%d",
              serverConnector.getProtocols().contains("ssl") ? "https" : "http",
              serverConnector.getHost() == null ? "localhost" : serverConnector.getHost(),
              serverConnector.getLocalPort()); })
        .toList();
  }

  /**
   * Convenience method for determining the current log level of the logging framework
   *
   * @return The log level
   */
  public Level getLogLevel() {
    return MutableLoggingProvider.getLevel();
  }

  /**
   * Method used in testing to obtain the database connection.
   *
   * @return The database connection
   */
  /* default */ Jdbi getJdbi() {
    return jdbi;
  }

  /**
   * Stops the Javalin server. The reason this method is called <code>close()</code>
   * and not <code>stop()</code> is for compliance with the
   * {@link java.lang.AutoCloseable} interface.
   */
  public void close() {
    if (app != null) {
      app.stop();
    }
  }

  /**
   * This method is used by the executable jar as the entrypoint for the
   * microservice. It starts the Fortune API service.
   *
   * @param args These arguments are not used at this time
   */
  public static void main(String[] args) {
    final FortuneApi service = new FortuneApi();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown initiated");
      service.close();
    }));

    service.run();
  }
}
