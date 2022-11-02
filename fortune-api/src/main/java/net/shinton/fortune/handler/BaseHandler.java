package net.shinton.fortune.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.factory.ObjectMapperFactory;
import net.shinton.fortune.model.RestResponseStatus;
import net.shinton.fortune.model.immutable.ImmutableRestResponse;
import net.shinton.util.BearerTokenTool;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the abstract base handler that contains all the common methods
 * used by handlers to authenticate and respond to requests.
 */
public abstract class BaseHandler implements Handler {
  private static final Logger log = LoggerFactory.getLogger(BaseHandler.class);
  private static final ObjectMapper mapper = new ObjectMapperFactory().newMapper();

  private final FortuneApiConfig config;

  /**
   * Initialized the base handler.
   *
   * @param config The API config
   */
  protected BaseHandler(FortuneApiConfig config) {
    this.config = config;
  }

  /**
   * Sets the response to an HTTP OK (200) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void ok(Context ctx) {
    response(ctx, HttpStatus.OK);
  }

  /**
   * Sets the response to an HTTP OK (200) response with a data payload.
   *
   * @param ctx The Javalin context
   * @param data The object to return
   */
  protected <T> void ok(Context ctx, T data) {
    response(ctx, HttpStatus.OK, RestResponseStatus.success, data, null);
  }

  /**
   * Sets the response to an HTTP CREATED (201) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void created(Context ctx) {
    response(ctx, HttpStatus.CREATED);
  }

  /**
   * Sets the response to an HTTP NO_CONTENT (204) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void noContent(Context ctx) {
    response(ctx, HttpStatus.NO_CONTENT);
  }

  /**
   * Sets the response to an HTTP NOT_FOUND (404) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void notFound(Context ctx) {
    response(ctx, HttpStatus.NOT_FOUND);
  }

  /**
   * Sets the response to an HTTP METHOD_NOT_ALLOWED (405) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void methodNotAllowed(Context ctx) {
    response(ctx, HttpStatus.METHOD_NOT_ALLOWED);
  }

  /**
   * Sets the response to an HTTP CONFLICT (409) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void conflict(Context ctx) {
    response(ctx, HttpStatus.CONFLICT);
  }

  /**
   * Sets the response to an HTTP BAD_REQUEST (400) response with a custom message.
   *
   * @param ctx The Javalin context
   * @param message The message
   */
  protected void badRequest(Context ctx, String message) {
    response(ctx, HttpStatus.BAD_REQUEST, RestResponseStatus.error, null, message);
  }

  /**
   * Sets the response to an HTTP UNAUTHORIZED (401) response with the default message.
   *
   * @param ctx The Javalin context
   */
  protected void unauthorized(Context ctx) {
    response(ctx, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Sets the response to an HTTP INTERNAL_SERVER_ERROR (500) response with a custom message.
   *
   * @param ctx The Javalin context
   * @param message The message
   */
  protected void internalServerError(Context ctx, String message) {
    response(ctx, HttpStatus.INTERNAL_SERVER_ERROR, RestResponseStatus.error, null, message);
  }

  /**
   * Sends a response back to the client with the specified HTTP status code.
   *
   * @param ctx The Javalin context
   * @param code The HTTP status code
   */
  protected void response(Context ctx, HttpStatus code) {
    int statusCode = code.getCode();
    RestResponseStatus responseStatus = (statusCode >= 200 && statusCode < 300) ?
        RestResponseStatus.success :
        RestResponseStatus.error;

    response(ctx, code, responseStatus, null, code.getMessage());
  }

  /**
   * Sends a response back to the client.
   *
   * @param ctx The Javalin context
   * @param statusCode The HTTP status code
   * @param status The overall status of the call (i.e. success or failure)
   * @param data The data payload
   * @param message The message
   */
  protected <T> void response(Context ctx, HttpStatus statusCode, RestResponseStatus status, T data, String message) {
    try {
      // Add a RestResponse to the context result
      ctx.status(statusCode)
          .result(mapper.writeValueAsString(ImmutableRestResponse.<T>builder()
              .status(status)
              .code(statusCode.getCode())
              .data(data)
              .message(message)
              .build()))
          .contentType(ContentType.APPLICATION_JSON);

    } catch (Exception e) {
      log.error("Error encoding response: " + data, e);
      /*
       * This is hardcoded this way so as not to cause a loop by calling the
       * same function over and over.
       */
      ctx.status(statusCode)
          .result("{\"status\":\"error\",\"code\":500,\"message\":\"Error encoding response\"}")
          .contentType(ContentType.APPLICATION_JSON);
    }
  }

  /**
   * Utility method to safely convert a <code>String</code> into a <code>Long</code>.
   *
   * @param value The string to convert
   * @return The converted value or <code>null</code> if it cannot be converted
   */
  protected Long getLong(String value) {
    if (StringUtils.isNumeric(value)) {
      try {
        return Long.parseLong(value);
      } catch (Exception e) {
        log.warn("Invalid format for long (%s)", value);
      }
    }

    return null;
  }

  /**
   * Utility method to safely convert a <code>String</code> into a <code>Integer</code>.
   *
   * @param value The string to convert
   * @return The converted value or <code>null</code> if it cannot be converted
   */
  protected Integer getInteger(String value) {
    if (StringUtils.isNumeric(value)) {
      try {
        return Integer.parseInt(value);
      } catch (Exception e) {
        log.warn("Invalid format for integer (%s)", value);
      }
    }

    return null;
  }

  /**
   * Utility method to safely convert a <code>String</code> into a <code>Instant</code>.
   *
   * @param value The string to convert
   * @return The converted value or <code>null</code> if it cannot be converted
   */
  protected Instant getInstant(String value) {
    if (StringUtils.isNotBlank(value)) {
      try {
        return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
      } catch (Exception e) {
        log.warn("Invalid format for date (%s)", value);
      }
    }

    return null;
  }

  /**
   * Utility method to safely convert a <code>String</code> into a <code>LocalDate</code>.
   *
   * @param value The string to convert
   * @return The converted value or <code>null</code> if it cannot be converted
   */
  protected LocalDate getLocalDate(String value) {
    if (StringUtils.isNotBlank(value)) {
      try {
        return LocalDate.parse(value);
      } catch (Exception e) {
        log.warn("Invalid format for date (%s)", value);
      }
    }

    return null;
  }

  /**
   * Checks to see if the user passed the proper bearer token for the API call.
   *
   * @param ctx The Javalin context
   * @param apiPath The API path
   * @return <code>true</code> if authorized
   */
  protected boolean isAuthorized(Context ctx, String apiPath) {
    BearerTokenTool tool = new BearerTokenTool(config.jwtSigningKey(), config.publicHost());
    return tool.validate(ctx.header("Authorization"), apiPath);
  }

  /**
   * Get the API config.
   *
   * @return The config
   */
  protected FortuneApiConfig getConfig() {
    return config;
  }

  /**
   * The entrypoint for the handler. This is the method Javalin calls when a new
   * request comes in.
   *
   * @param ctx The Javalin context
   * @throws Exception When there is an unexpected error
   */
  public void handle(@NotNull Context ctx) throws Exception {
    try {
      /*
       * The reason for the abstraction is to build in exception handling.
       */
      handleRequest(ctx);

    } catch (Exception e) {
      log.error("Internal error while handling request", e);
      internalServerError(ctx, e.getMessage());
    }
  }

  /**
   * This is the method subclasses must implement to handle Javalin requests.
   *
   * @param ctx The Javalin context
   * @throws Exception When there is an unexpected error
   */
  protected abstract void handleRequest(@NotNull Context ctx) throws Exception;

  /**
   * Examines the request header to determine the IP address of the caller,
   * taking proxies into account.
   *
   * @param ctx The Javalin context
   * @return Our best guess for the caller IP
   */
  protected String getCallerAddress(Context ctx) {
    String address = ctx.header("X-Forwarded-For");

    if (StringUtils.isBlank(address)) {
      address = ctx.ip();
    }

    return address;
  }
}
