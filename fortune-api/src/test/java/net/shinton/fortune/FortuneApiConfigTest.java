package net.shinton.fortune;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import net.shinton.fortune.factory.FortuneApiConfigFactory;
import org.junit.Test;

/**
 * <code>JUnit</code> tests for the {@link FortuneApiConfig} class.
 */
public class FortuneApiConfigTest {
  /**
   * Test to ensure compatibility with the <code>Owner</code> library.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testOwner() throws Exception {
    FortuneApiConfig config = new FortuneApiConfigFactory().getConfig();

    assertNotNull(config);
    assertEquals("INFO", config.logLevel());
    assertEquals("jdbc:sqlite:/fortune-data/fortune-api.db", config.jdbcUrl());
    assertEquals(80, config.port());
    assertEquals(443, config.sslPort());
    assertNull(config.sslKey());
    assertNull(config.sslCerts());
    assertNull(config.jwtSigningKey());
    assertNull(config.publicHost());
  }

  /**
   * Test to ensure compatibility with the <code>Immutables</code> framework.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testImmutable() throws Exception {
    FortuneApiConfig config = ImmutableFortuneApiConfig.builder()
        .logLevel("ERROR")
        .jdbcUrl("jdbc")
        .port(123)
        .sslPort(456)
        .sslKey("abc")
        .sslCerts("efg")
        .jwtSigningKey("hij")
        .publicHost("http://localhost")
        .build();

    assertNotNull(config);
    assertEquals("ERROR", config.logLevel());
    assertEquals("jdbc", config.jdbcUrl());
    assertEquals(123, config.port());
    assertEquals(456, config.sslPort());
    assertEquals("abc", config.sslKey());
    assertEquals("efg", config.sslCerts());
    assertEquals("hij", config.jwtSigningKey());
    assertEquals("http://localhost", config.publicHost());

    config = ImmutableFortuneApiConfig.builder()
        .logLevel("ERROR")
        .jdbcUrl("jdbc")
        .port(123)
        .sslPort(456)
        .jwtSigningKey("hij")
        .publicHost("http://localhost")
        .build();

    assertNull(config.sslKey());
    assertNull(config.sslCerts());
  }
}