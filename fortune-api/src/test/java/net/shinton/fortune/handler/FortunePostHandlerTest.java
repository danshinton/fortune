package net.shinton.fortune.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.Locale;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.factory.FortuneDatabaseFactory;
import net.shinton.fortune.factory.ObjectMapperFactory;
import net.shinton.fortune.model.RestResponseStatus;
import net.shinton.fortune.model.immutable.RestResponse;
import net.shinton.util.BearerTokenTool;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * <code>JUnit</code> tests for the {@link FortunePostHandler} class.
 */
@SuppressWarnings("CPD-START")
public class FortunePostHandlerTest {
  private static final ObjectMapper mapper = new ObjectMapperFactory().newMapper();

  private Jdbi jdbi;

  /**
   * Create a clean in-memory database for each test
   */
  @Before
  public void beforeTest() {
    this.jdbi = new FortuneDatabaseFactory().newDatabase("jdbc:sqlite::memory:");
  }

  /**
   * "Happy path" test for the {@link FortunePostHandler}.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testSuccess() throws Exception {
    String fortune = "Test first for success!";

    // Create a bearer token for authentication
    String publicHost = "fortune.shinton.net";
    String signingKey = BearerTokenTool.newSigningKey();
    BearerTokenTool bearerTokenTool = new BearerTokenTool(signingKey, publicHost);
    String bearerToken = bearerTokenTool.generate(1000, "/api/v1/fortune");

    // Mock a context so the handler thinks Javalin called it
    Context context = mock(Context.class);
    when(context.ip()).thenReturn("0.0.0.0");
    when(context.status(any())).thenReturn(context);
    when(context.result(anyString())).thenReturn(context);
    when(context.contentType(ContentType.APPLICATION_JSON)).thenReturn(context);
    when(context.body()).thenReturn(String.format(Locale.ROOT, "{ \"fortune\": \"%s\" }", fortune));
    when(context.header("Authorization")).thenReturn(bearerToken);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);
    when(config.publicHost()).thenReturn(publicHost);
    when(config.jwtSigningKey()).thenReturn(signingKey);

    // Verify the fortune is not already in the database
    assertEquals(0, getCount(fortune));

    // Make the call
    FortunePostHandler handler = new FortunePostHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<?> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.CREATED.getCode(), response.getCode());

    // Verify the fortune was added to the database
    assertEquals(1, getCount(fortune));
  }

  /**
   * Test a call that does not have proper authorization.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testUnauthorized() throws Exception {
    String fortune = "Test first for success!";

    // Create an invalid bearer token using the wrong path for authentication
    String publicHost = "fortune.shinton.net";
    String signingKey = BearerTokenTool.newSigningKey();
    BearerTokenTool bearerTokenTool = new BearerTokenTool(signingKey, publicHost);
    String bearerToken = bearerTokenTool.generate(1000, "/api/v1/nope");

    // Mock a context so the handler thinks Javalin called it
    Context context = mock(Context.class);
    when(context.ip()).thenReturn("0.0.0.0");
    when(context.status(any())).thenReturn(context);
    when(context.result(anyString())).thenReturn(context);
    when(context.contentType(ContentType.APPLICATION_JSON)).thenReturn(context);
    when(context.body()).thenReturn(String.format(Locale.ROOT, "{ \"fortune\": \"%s\" }", fortune));
    when(context.header("Authorization")).thenReturn(bearerToken);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);
    when(config.publicHost()).thenReturn(publicHost);
    when(config.jwtSigningKey()).thenReturn(signingKey);

    // Verify the fortune is not already in the database
    assertEquals(0, getCount(fortune));

    // Make the call
    FortunePostHandler handler = new FortunePostHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<?> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.error, response.getStatus());
    assertEquals(HttpStatus.UNAUTHORIZED.getCode(), response.getCode());

    // Make sure the fortune was not added
    assertEquals(0, getCount(fortune));
  }

  /**
   * Test to ensure we cannot add duplicates to the database.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testInsertDuplicate() throws Exception {
    String fortune = "Sometimes two is not better than one.";

    // Create a bearer token for authentication
    String publicHost = "fortune.shinton.net";
    String signingKey = BearerTokenTool.newSigningKey();
    BearerTokenTool bearerTokenTool = new BearerTokenTool(signingKey, publicHost);
    String bearerToken = bearerTokenTool.generate(1000, "/api/v1/fortune");

    // Mock a context so the handler thinks Javalin called it
    Context context = mock(Context.class);
    when(context.ip()).thenReturn("0.0.0.0");
    when(context.status(any())).thenReturn(context);
    when(context.result(anyString())).thenReturn(context);
    when(context.contentType(ContentType.APPLICATION_JSON)).thenReturn(context);
    when(context.body()).thenReturn(String.format(Locale.ROOT, "{ \"fortune\": \"%s\" }", fortune));
    when(context.header("Authorization")).thenReturn(bearerToken);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);
    when(config.publicHost()).thenReturn(publicHost);
    when(config.jwtSigningKey()).thenReturn(signingKey);

    // Verify the fortune is not already in the database
    assertEquals(0, getCount(fortune));

    // Make the call
    FortunePostHandler handler = new FortunePostHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<?> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.CREATED.getCode(), response.getCode());

    // Verify the fortune was added to the database
    assertEquals(1, getCount(fortune));

    // Try to add it again and expect a CONFLICT
    handler.handle(context);

    resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(2)).result(resultCaptor.capture());
    response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.error, response.getStatus());
    assertEquals(HttpStatus.CONFLICT.getCode(), response.getCode());

    // Verify the fortune was not added again to the database
    assertEquals(1, getCount(fortune));
  }

  /**
   * Test to ensure SQL injection does not occur.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testSqlInjection() throws Exception {
    String fortune = "I think I am sneaky'; DELETE FROM fortune; --";

    // Create a bearer token for authentication
    String publicHost = "fortune.shinton.net";
    String signingKey = BearerTokenTool.newSigningKey();
    BearerTokenTool bearerTokenTool = new BearerTokenTool(signingKey, publicHost);
    String bearerToken = bearerTokenTool.generate(1000, "/api/v1/fortune");

    // Mock a context so the handler thinks Javalin called it
    Context context = mock(Context.class);
    when(context.ip()).thenReturn("0.0.0.0");
    when(context.status(any())).thenReturn(context);
    when(context.result(anyString())).thenReturn(context);
    when(context.contentType(ContentType.APPLICATION_JSON)).thenReturn(context);
    when(context.body()).thenReturn(String.format(Locale.ROOT, "{ \"fortune\": \"%s\" }", fortune));
    when(context.header("Authorization")).thenReturn(bearerToken);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);
    when(config.publicHost()).thenReturn(publicHost);
    when(config.jwtSigningKey()).thenReturn(signingKey);

    // Ensure we have fortunes in the database
    assertTrue(getCount() > 0);

    // Verify the fortune is not already in the database
    assertEquals(0, getCount(fortune));

    // Make the call
    FortunePostHandler handler = new FortunePostHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<?> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.CREATED.getCode(), response.getCode());

    // Ensure we still have fortunes in the database
    assertTrue(getCount() > 0);

    // Verify the fortune was added to the database
    assertEquals(1, getCount(fortune));
  }

  /**
   * Utility method to get the number of occurrences of a particular fortune in
   * the database.
   *
   * @param fortune The fortune to check
   * @return The count
   */
  private int getCount(String fortune) {
    return jdbi.withHandle(handle ->
        handle.createQuery("SELECT COUNT(*) FROM fortune WHERE quote = :fortune")
            .bind("fortune", fortune)
            .mapTo(int.class)
            .first());
  }

  /**
   * Utility method to get the total number of fortunes in the database.
   *
   * @return The total number of fortunes
   */
  private int getCount() {
    return jdbi.withHandle(handle ->
        handle.createQuery("SELECT COUNT(*) FROM fortune")
            .mapTo(int.class)
            .first());
  }
}
