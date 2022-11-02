package net.shinton.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * <code>JUnit</code> tests for the {@link BearerTokenTool} class.
 */
public class BearerTokenToolTest {
  private static final String FORTUNE_PUBLIC_HOST = "fortune.shinton.net";
  private static final String API_PATH = "/api/v1/fortune";

  /**
   * "Happy path" test for the {@link BearerTokenTool}.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testSuccess() throws Exception {
    BearerTokenTool tool = new BearerTokenTool(BearerTokenTool.newSigningKey(), FORTUNE_PUBLIC_HOST);
    String token = tool.generate(1000, API_PATH);
    assertTrue(StringUtils.isNotBlank(token));
    assertTrue(tool.validate(token, API_PATH));
  }

  /**
   * Test to ensure validation rejects bad tokens
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testFailure() throws Exception {
    BearerTokenTool tool = new BearerTokenTool(BearerTokenTool.newSigningKey(), FORTUNE_PUBLIC_HOST);

    // Test expired
    String token = tool.generate(-1000, API_PATH);
    assertTrue(StringUtils.isNotBlank(token));
    assertFalse(tool.validate(token, API_PATH));

    // Test bad path
    token = tool.generate(1000, API_PATH);
    assertFalse(tool.validate(token, StringUtils.reverse(API_PATH)));

    // Test missing "Bearer"
    assertFalse(tool.validate(token.substring(7), API_PATH));

    // Test corrupt token
    assertFalse(tool.validate("Bearer " + RandomStringUtils.random(token.length() - 7), API_PATH));
  }
}