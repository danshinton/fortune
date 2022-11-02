package net.shinton.fortune.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import java.util.Map;
import net.shinton.fortune.FortuneApiConfig;
import net.shinton.fortune.factory.FortuneDatabaseFactory;
import net.shinton.fortune.factory.ObjectMapperFactory;
import net.shinton.fortune.model.RestResponseStatus;
import net.shinton.fortune.model.immutable.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * <code>JUnit</code> tests for the {@link FortuneGetHandler} class.
 */
public class FortuneGetHandlerTest {
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
   * "Happy path" test for the {@link FortuneGetHandler}.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testSuccess() throws Exception {
    // Create a bearer token for authentication
    Context context = mock(Context.class);
    when(context.ip()).thenReturn("0.0.0.0");
    when(context.status(any())).thenReturn(context);
    when(context.result(anyString())).thenReturn(context);
    when(context.contentType(ContentType.APPLICATION_JSON)).thenReturn(context);

    // Mock a config
    FortuneApiConfig config = mock(FortuneApiConfig.class);

    // Make the call
    FortuneGetHandler handler = new FortuneGetHandler(config, jdbi);
    handler.handle(context);

    // Capture and validate the response
    ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
    verify(context, times(1)).result(resultCaptor.capture());
    RestResponse<Map<String, String>> response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.OK.getCode(), response.getCode());

    Map<String, String> data = response.getData();
    assertNotNull(data);

    assertTrue(StringUtils.isNotBlank(data.get("fortune")));

    // Call it again to see if we get another fortune
    handler.handle(context);

    verify(context, times(2)).result(resultCaptor.capture());
    response = mapper.readValue(resultCaptor.getValue(), new TypeReference<>() {});

    assertEquals(RestResponseStatus.success, response.getStatus());
    assertEquals(HttpStatus.OK.getCode(), response.getCode());

    data = response.getData();
    assertNotNull(data);

    assertTrue(StringUtils.isNotBlank(data.get("fortune")));
  }
}
