package net.shinton.fortune.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import net.shinton.fortune.client.factory.FortuneApiClientConfigFactory;
import org.junit.Test;

/**
 * <code>JUnit</code> tests for the {@link FortuneApiClientConfig} class.
 */
public class FortuneApiClientConfigTest {
  /**
   * Test to ensure compatibility with the <code>Owner</code> library.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testOwner() throws Exception {
    // Get config
    FortuneApiClientConfig config = new FortuneApiClientConfigFactory().getConfig();

    // Ensure default values are set
    assertNotNull(config);
    assertNull(config.baseUrl());
    assertEquals(Long.valueOf(10000), config.connectTimeout());
    assertEquals(Long.valueOf(10000), config.readTimeout());
    assertEquals(Long.valueOf(10000), config.writeTimeout());
    assertNull(config.sslCerts());
  }

  /**
   * Test to ensure compatibility with the <code>Immutables</code> framework.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testImmutable() throws Exception {
    // Get config
    FortuneApiClientConfig config = ImmutableFortuneApiClientConfig.builder()
        .baseUrl("hello")
        .connectTimeout(1L)
        .readTimeout(2L)
        .writeTimeout(3L)
        .sslCerts("there")
        .build();

    // Ensure values are set
    assertNotNull(config);
    assertEquals("hello", config.baseUrl());
    assertEquals(Long.valueOf(1), config.connectTimeout());
    assertEquals(Long.valueOf(2), config.readTimeout());
    assertEquals(Long.valueOf(3), config.writeTimeout());
    assertEquals("there", config.sslCerts());

    // Test nullable fields
    config = ImmutableFortuneApiClientConfig.builder()
        .baseUrl("hello")
        .connectTimeout(1L)
        .readTimeout(2L)
        .writeTimeout(3L)
        .build();

    assertNull(config.sslCerts());
  }
}