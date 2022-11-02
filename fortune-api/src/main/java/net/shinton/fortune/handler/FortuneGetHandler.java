package net.shinton.fortune.handler;

import io.javalin.http.Context;
import java.util.Map;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.data.FortuneModel;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin handler for requests to get a random fortune.
 */
public class FortuneGetHandler extends BaseHandler {
  private static final Logger log = LoggerFactory.getLogger(FortuneGetHandler.class);

  private Jdbi jdbi;

  /**
   * Creates a new get handler.
   *
   * @param config The API config
   * @param jdbi The database to query
   */
  public FortuneGetHandler(FortuneApiConfig config, Jdbi jdbi) {
    super(config);
    this.jdbi = jdbi;
  }

  /**
   * Handles the request.
   *
   * @param ctx The Javalin context
   * @throws Exception When there is an unexpected error
   */
  @Override
  protected void handleRequest(@NotNull Context ctx) throws Exception {
    String callerAddress = getCallerAddress(ctx);

    if (log.isTraceEnabled()) {
      log.trace("FortuneGetHandler ({})", callerAddress);
    }

    String fortune = new FortuneModel(jdbi).getRandomFortune();
    ok(ctx, Map.of("fortune", fortune));
  }
}
