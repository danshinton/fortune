package net.shinton.fortune.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import java.util.Map;
import net.shinton.exception.DuplicateEntryException;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.data.FortuneModel;
import net.shinton.fortune.factory.ObjectMapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin handler for requests to add a fortune.
 */
public class FortunePostHandler extends BaseHandler {
  private static final Logger log = LoggerFactory.getLogger(FortunePostHandler.class);
  private static final ObjectMapper mapper = new ObjectMapperFactory().newMapper();

  private Jdbi jdbi;

  /**
   * Creates a new post handler.
   *
   * @param config The API config
   * @param jdbi The database to query
   */
  public FortunePostHandler(FortuneApiConfig config, Jdbi jdbi) {
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
      log.trace("FortunePutHandler ({})", callerAddress);
    }

    if (!isAuthorized(ctx, "/api/v1/fortune")) {
      log.warn("User not authorized to PUT new fortune ({})", callerAddress);
      unauthorized(ctx);
      return;
    }

    // Get the fortune to add from the body of the request
    Map<String, String> json = mapper.readValue(ctx.body(), new TypeReference<>() {});

    String fortune = json.get("fortune");
    if (StringUtils.isBlank(fortune)) {
      badRequest(ctx, "Parameter 'fortune' is required");
    }

    try {
      new FortuneModel(jdbi).addFortune(fortune);
      created(ctx);

    } catch (DuplicateEntryException e) {
      log.info("Add failed due to duplicate fortune: {}", fortune);
      conflict(ctx);
    }
  }
}
