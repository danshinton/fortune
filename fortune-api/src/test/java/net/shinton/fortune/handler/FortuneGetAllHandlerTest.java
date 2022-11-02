package net.shinton.fortune.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import java.util.List;
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
 * <code>JUnit</code> tests for the {@link FortuneGetAllHandler} class.
 */
public class FortuneGetAllHandlerTest {
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
   * "Happy path" test for the {@link FortuneGetAllHandler}.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testSuccess() throws Exception {
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
    when(context.header("Authorization")).thenReturn(bearerToken);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);
    when(config.publicHost()).thenReturn(publicHost);
    when(config.jwtSigningKey()).thenReturn(signingKey);

    // Make the call
    FortuneGetAllHandler handler = new FortuneGetAllHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<List<String>> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.OK.getCode(), response.getCode());

    // Ensure all the fortunes were sent
    List<String> fortunes = response.getData();
    assertNotNull(fortunes);
    assertEquals(getCount(), fortunes.size());
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
