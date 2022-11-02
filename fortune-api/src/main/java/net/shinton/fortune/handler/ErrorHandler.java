package net.shinton.fortune.handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Map;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.model.RestResponseStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Javalin handler for errant requests
 */
public class ErrorHandler extends BaseHandler {
  /**
   * Creates a new error handler.
   *
   * @param config The API config
   */
  public ErrorHandler(FortuneApiConfig config) {
    super(config);
  }

  /**
   * Handles the request.
   *
   * @param ctx The Javalin context
   * @throws Exception When there is an unexpected error
   */
  @Override
  protected void handleRequest(@NotNull Context ctx) throws Exception {
    // Send back an error message that includes the path for debug purposes
    HttpStatus status = ctx.status();
    Map<String, String> data = Map.of(
        "path", ctx.path()
    );
    response(ctx, status, RestResponseStatus.error, data, status.getMessage() + " (Unhandled)");
  }
}
