package net.shinton.fortune.handler;

import io.javalin.http.Context;
import java.util.List;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.data.FortuneModel;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin handler for requests to get all fortunes.
 */
public class FortuneGetAllHandler extends BaseHandler {
  private static final Logger log = LoggerFactory.getLogger(FortuneGetAllHandler.class);

  private Jdbi jdbi;

  /**
   * Creates a new get all handler.
   *
   * @param config The API config
   * @param jdbi The database to query
   */
  public FortuneGetAllHandler(FortuneApiConfig config, Jdbi jdbi) {
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

    if (!isAuthorized(ctx, "/api/v1/fortune")) {
      log.warn("User not authorized to GET all fortunes ({})", callerAddress);
      unauthorized(ctx);
      return;
    }

    List<String> fortunes = new FortuneModel(jdbi).getAllFortunes();
    ok(ctx, fortunes);
  }
}
