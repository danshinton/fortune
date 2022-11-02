package net.shinton.fortune;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import net.shinton.fortune.client.FortuneApiClient;
import net.shinton.fortune.client.FortuneApiClientConfig;
import net.shinton.fortune.client.factory.FortuneApiClientFactory;
import net.shinton.util.BearerTokenTool;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <p>
 *   <code>JUnit</code> integration tests for the {@link FortuneApiClient} class.
 * </p>
 * <p>
 *   These tests start a local Fortune API server on a random port with an
 *   in-memory database to make calls against. This allows a real end-to-end
 *   test to be done to ensure the client works as designed.
 * </p>>
 */
public class FortuneApiClientTest {
  private static FortuneApiConfig apiConfig;
  private static final String API_PATH = "/api/v1/fortune";
  private static final String PUBLIC_HOST = "localhost";
  private static String bearerToken;

  private static final String CERTS =
      "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURpVENDQW5HZ0F3SUJBZ0lKQUxHM" +
      "UxrYlFrQWdKTUEwR0NTcUdTSWIzRFFFQkN3VUFNSGN4Q3pBSkJnTlYKQkFZVEFsVlRNUT" +
      "B3Q3dZRFZRUUlEQVJQYUdsdk1ROHdEUVlEVlFRSERBWlZjbUpoYm1FeEZEQVNCZ05WQkF" +
      "vTQpDMFJoYmlCVGFHbHVkRzl1TVJRd0VnWURWUVFMREF0RmJtZHBibVZsY21sdVp6RWNN" +
      "Qm9HQTFVRUF3d1RabTl5CmRIVnVaUzV6YUdsdWRHOXVMbTVsZERBZUZ3MHlNakV3TXpFe" +
      "E1qTXlOVEJhRncwek1qRXdNamd4TWpNeU5UQmEKTUhjeEN6QUpCZ05WQkFZVEFsVlRNUT" +
      "B3Q3dZRFZRUUlEQVJQYUdsdk1ROHdEUVlEVlFRSERBWlZjbUpoYm1FeApGREFTQmdOVkJ" +
      "Bb01DMFJoYmlCVGFHbHVkRzl1TVJRd0VnWURWUVFMREF0RmJtZHBibVZsY21sdVp6RWNN" +
      "Qm9HCkExVUVBd3dUWm05eWRIVnVaUzV6YUdsdWRHOXVMbTVsZERDQ0FTSXdEUVlKS29aS" +
      "Wh2Y05BUUVCQlFBRGdnRVAKQURDQ0FRb0NnZ0VCQU5SSEFBa2NNR3JRbHduZ0pPekIwOC" +
      "tCYnZQY3hWbVFNaC9jQjQ2cjh6d2JBbkttTGk5bwpoT0dSZUc3RFNNQkZ0eU1FeEtFUk1" +
      "nREhtWS9qK01vR3YySDVqNTZ0eE55VkVXa3BzZ0FpZkEzUVBMRFIvRzN2CjdacWdTTTlx" +
      "SUFqOE5XaVVsRXNyb3FmQWU3cmRUbTdoRnFWMVU5NGdiSWRyaXMwYkx5U0NQQ3lMWE54b" +
      "DBYNmsKM1lrMU1HRUdJckdTWFFjbmV5U0VUM1JrQTRQTVRNNER4V04wemdMZjRZRXU4aE" +
      "1oSmVRTmt5ZVNJNm5DaTk4MgpIaTVrdC9PbTNkYUN2bUhIS2lhZWlDcm5WWjhtWHpVQjJ" +
      "veHlYcy92MWlQYmRIbTgwWjVWY1J1YnYvS3lSVHVwCkNtY1NDbXpiUUhXSUlMelJrM1Iz" +
      "blhhL29vV2xobmRVZ0FrQ0F3RUFBYU1ZTUJZd0ZBWURWUjBSQkEwd0M0SUoKYkc5allXe" +
      "G9iM04wTUEwR0NTcUdTSWIzRFFFQkN3VUFBNElCQVFCUFphdkIyOWtZQ0lNL1BoaGJBWm" +
      "NwVFZjLworK0RSeFR2a3ZQbGE1NkgyM01acDYyaUR3dTJGZFg2akFReGJ5SExJTlpjaEN" +
      "VeEh1ZldwaDVwUXNJZDdWRXJJCjYvaXJHMUJtR2tXWFk4WjYwWHR0dUk2ZGZxTGhrS2Nk" +
      "TkkvZDJXbStWdTRWZ1UxZVRua1p4eWhFVFNsWWZYMUoKbW1peWlsUkg1S2gwV2xJUFJvV" +
      "0N4K2VKTks2SklDbWJIdmh2TE0vdWhpRWU4ME9icUdoazhlcmtTYkF4dEgrUQpXOC90SF" +
      "k4ZmwvaVVMM2lBalh3RTJaTXUzWGZjSWZzbUhlNTIrcDVaQXMxbVJhMUREa1FleDN0dVR" +
      "3akJja0V2CkVoUlQyb0Y2MkNPWGllSlU0MGpXMllVR29sWVpnbitaVGxKT2RyOEhFU2Vp" +
      "V21ScTNXR1NQMTRTV3dFawotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";

  private static final String KEY =
      "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQ" +
      "kFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRRFVSd0FKSERCcTBKY0oKNENUc3dkUFBnVz" +
      "d6M01WWmtESWYzQWVPcS9NOEd3SnlwaTR2YUlUaGtYaHV3MGpBUmJjakJNU2hFVElBeDV" +
      "tUAo0L2pLQnI5aCtZK2VyY1RjbFJGcEtiSUFJbndOMER5dzBmeHQ3KzJhb0VqUGFpQUkv" +
      "RFZvbEpSTEs2S253SHU2CjNVNXU0UmFsZFZQZUlHeUhhNHJOR3k4a2dqd3NpMXpjWmRGK" +
      "3BOMkpOVEJoQmlLeGtsMEhKM3NraEU5MFpBT0QKekV6T0E4VmpkTTRDMytHQkx2SVRJU1" +
      "hrRFpNbmtpT3B3b3ZmTmg0dVpMZnpwdDNXZ3I1aHh5b21ub2dxNTFXZgpKbDgxQWRxTWN" +
      "sN1A3OVlqMjNSNXZOR2VWWEVibTcveXNrVTdxUXBuRWdwczIwQjFpQ0M4MFpOMGQ1MTJ2" +
      "NktGCnBZWjNWSUFKQWdNQkFBRUNnZ0VCQUllT3E1UWx2em1HNk56eW1VSGo2Um43QXRxU" +
      "jRpajJyenV2SzloTk1DL3kKNDVaSlR2Y1JYTUliUG5nbEZ3Rkp3OUNHQy9iN1h4NG1XSW" +
      "NOd3VOb2ViRGNTSDNCWkJTazBSQ0NBdHlaS1BVOQpSSFI2TkNRVWNud3EzVFF2QmpRV3N" +
      "4aU13VUpicEN4UisvRnh2OU9JeEFoM0swNlBFQjlPYXIzZUhUWElRUnNECjNVYXVUTlNk" +
      "RWdreGFLYjlzVGhvZmh2OUoreWNXWnhTbWxRblltN1VycVBVbFg0ZXhaaDB4T0tFMkZ5a" +
      "U15Z3EKaUhmSEFtRHJLcmNjSXJsV2NoWjFIWE9JbVpLSnVGbW0rOURwcE5MbU1UOWp3U0" +
      "dwWXphWU9SaGFhTCs2dkczQQpOWjNMNDE1NTRqdjBKZW1MTWJYQzVrYkF5SkpVSVFhR3R" +
      "DcTZlcVpYMkxFQ2dZRUEvYVVOYm9vTXFEdFZIaytKCmlVdGd2OSsvS3VDZ3p6Q2l5UjIx" +
      "cU9Kd1NXOUx2b1VZR3k1T284NjhPWVFQaVNoZFZpNjQ0MFJ2ckoxNHVUcVUKVU40TzFNZ" +
      "ldxY0tGRUlTNWkxSis5dzlmSERjTnVDRXFaQ2NRM2l4NVpHNWlSbWlHNDZTdWp2WlU5V1" +
      "ZMajRSQQptdmw3b2d6N095dmgzbTRHRUMveGo5OTZBeTBDZ1lFQTFqK2NvVTVSaEZQTE0" +
      "xWDgrdkIwNitnVlUxZnJnMHRuCjRWQXNWdkYvSVZpMWtodXVjS2tsczBmUXRGdjJ4ZjRl" +
      "alMxdDRLa2cxNHZIUnFyZnZ6UVN4cXorVUJ2eXltaTIKdlZCVllWcVltRGNoOWdkeUFzL" +
      "3RVNzFvZzB5WHI5L2lXRHBQQ29SbURpNXg2Z3lWVVNnZTVQYldFdCs2NzE5TApJRVVERT" +
      "lJczZjMENnWUEyaHlvcERtS0VOQ1VyVy92OFdRa1dscnBaTkdzOXYzM3ZjSVNpSnZQdXh" +
      "ZOXFDT0RTCnp4UzI0SFhVZzVCM2N3Y0Z4UXVZU0JrZDZjaVBRWW1yRE9IeFduaktpL2Zo" +
      "UWpkRlBWbndNUXpJV3dtSEFSSkUKNzZVUHJrMzJpa2gwVEhwYkxBY2UwdFFXNFV6cU1Jd" +
      "FhCYlZQZEN6NlhvZXpNc0g0N3VXbWdXdVFqUUtCZ0YrQgpEN01nRkZwbWQrZzBab2I5OV" +
      "VETHZlWGZCVThMcUF6YURrbmJjUTVPV3ZIOGdQQWJsb0NxZkxCSlptR3YrN0ozCkU4blg" +
      "ycG42NXlQck83NGgybFRSL0xOOUllZDBjakZGeENtWGx2b1RhWUlnbzRQNWFCT3lGUXFp" +
      "YytPa2EyTmsKNnFNSGplNjRhWE1wbE00T2ttY0NXVDhrbUZIb0tyY0J6SE5qZHozcEFvR" +
      "0JBTzN3U0thMFUvOTZmMGVyMFZ5RgpKVC9KeFpiMFF4R2tEMWhNM3BEOGIxV21HMm13S0" +
      "1LRUtydXpzcmR6dit0dUdJUmF3T1gzOVo4STlwZWpJczVUCmtkbk9LUTdmRHV4bmpPTmN" +
      "RSG9QOTE5MDcvU3F4c3daKzJmZEUxc0JnbEEvdlcrOUNLWmFyUFFJQmRwaEhtemMKYzl1" +
      "NHZFVzB4Y3ptTStSSmY1MVhVU3FuCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";

  /**
   * Creates the API configuration used by all the test methods.
   */
  @BeforeClass
  public static void beforeClass() {
    // Generate a new signing key and bearerToken for this test
    String signingKey = BearerTokenTool.newSigningKey();

    bearerToken = new BearerTokenTool(signingKey, PUBLIC_HOST)
        .generate(300, API_PATH);

    /*
     * Configure the API to create a server running on a random port with an
     * in-memory database and self-signed certificate.
     */
    apiConfig = mock(FortuneApiConfig.class);
    when(apiConfig.logLevel()).thenReturn("INFO");
    when(apiConfig.jdbcUrl()).thenReturn("jdbc:sqlite::memory:");
    when(apiConfig.port()).thenReturn(0); // Let Java assign a random free port
    when(apiConfig.sslKey()).thenReturn(KEY);
    when(apiConfig.sslCerts()).thenReturn(CERTS);
    when(apiConfig.jwtSigningKey()).thenReturn(signingKey);
    when(apiConfig.publicHost()).thenReturn(PUBLIC_HOST);
  }

  /**
   * Tests the {@link FortuneApiClient#getFortune()} method.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testGetFortune() throws Exception {
    try (FortuneApi fortuneApi = new FortuneApi(apiConfig)) {
      // Run the server
      fortuneApi.run();

      // Get the randomly assigned address
      List<String> localAddresses = fortuneApi.getLocalAddresses();

      /*
       * This should only return one address based on the current logic, but
       * the test is written to work if multiple addresses are found.
       */
      for (String localAddress : localAddresses) {
        // Create a new client
        FortuneApiClientConfig clientConfig = mock(FortuneApiClientConfig.class);
        when(clientConfig.baseUrl()).thenReturn(localAddress);
        when(clientConfig.sslCerts()).thenReturn(CERTS);

        FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(clientConfig);

        // Try to get some fortunes
        assertTrue(StringUtils.isNotBlank(client.getFortune()));
        assertTrue(StringUtils.isNotBlank(client.getFortune()));
        assertTrue(StringUtils.isNotBlank(client.getFortune()));
      }
    }
  }

  /**
   * Tests the {@link FortuneApiClient#addFortune(String)} method.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testAddFortune() throws Exception {
    try (FortuneApi fortuneApi = new FortuneApi(apiConfig)) {
      // Run the server
      fortuneApi.run();

      // Get the database connection for verification
      Jdbi jdbi = fortuneApi.getJdbi();

      // Get the randomly assigned address
      List<String> localAddresses = fortuneApi.getLocalAddresses();

      /*
       * This should only return one address based on the current logic, but
       * the test is written to work if multiple addresses are found.
       */
      for (String localAddress : localAddresses) {
        // Create a new client
        FortuneApiClientConfig clientConfig = mock(FortuneApiClientConfig.class);
        when(clientConfig.baseUrl()).thenReturn(localAddress);
        when(clientConfig.sslCerts()).thenReturn(CERTS);

        FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(clientConfig);
        client.updateBearerToken(bearerToken);

        // Create a unique fortune
        String fortune = String.format(Locale.ROOT, "Testing is necessary for a happy life (%s)", localAddress);

        // Query the database to ensure it does not already exist
        assertEquals(0, getCount(jdbi, fortune));

        // Add the fortune
        assertTrue(client.addFortune(fortune));

        // Query the database to ensure it was added
        assertEquals(1, getCount(jdbi, fortune));
      }
    }
  }

  /**
   * Tests the {@link FortuneApiClient#addFortune(String)} method without proper
   * authentication to ensure auth is working.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testAddFortuneWithoutAuth() throws Exception {
    try (FortuneApi fortuneApi = new FortuneApi(apiConfig)) {
      // Run the server
      fortuneApi.run();

      // Get the database connection for verification
      Jdbi jdbi = fortuneApi.getJdbi();

      // Get the randomly assigned address
      List<String> localAddresses = fortuneApi.getLocalAddresses();

      /*
       * This should only return one address based on the current logic, but
       * the test is written to work if multiple addresses are found.
       */
      for (String localAddress : localAddresses) {
        // Create a new client without a bearer token
        FortuneApiClientConfig clientConfig = mock(FortuneApiClientConfig.class);
        when(clientConfig.baseUrl()).thenReturn(localAddress);
        when(clientConfig.sslCerts()).thenReturn(CERTS);

        FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(clientConfig);

        // Create a unique fortune
        String fortune = String.format(Locale.ROOT, "Testing is necessary for a happy life (%s)", localAddress);

        // Query the database to ensure it does not already exist
        assertEquals(0, getCount(jdbi, fortune));

        try {
          // Try to add the fortune
          client.addFortune(fortune);

          // If we get here, then bad auth was accepted
          fail();

        } catch (IOException e) {
          assertTrue(e.getMessage().contains("Unauthorized"));
        }

        // Query the database to ensure it was not added
        assertEquals(0, getCount(jdbi, fortune));
      }
    }
  }

  /**
   * Tests the {@link FortuneApiClient#getAllFortunes()} method.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testGetAll() throws Exception {
    try (FortuneApi fortuneApi = new FortuneApi(apiConfig)) {
      // Run the server
      fortuneApi.run();

      // Get the database connection for verification
      Jdbi jdbi = fortuneApi.getJdbi();

      // Get the randomly assigned address
      List<String> localAddresses = fortuneApi.getLocalAddresses();

      /*
       * This should only return one address based on the current logic, but
       * the test is written to work if multiple addresses are found.
       */
      for (String localAddress : localAddresses) {
        // Create a new client
        FortuneApiClientConfig clientConfig = mock(FortuneApiClientConfig.class);
        when(clientConfig.baseUrl()).thenReturn(localAddress);
        when(clientConfig.sslCerts()).thenReturn(CERTS);

        FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(clientConfig);
        client.updateBearerToken(bearerToken);

        // Get a count of how many fortunes to expect
        int count = getCount(jdbi);

        // Get the fortunes
        List<String> fortunes = client.getAllFortunes();

        // Ensure we got all of them
        assertNotNull(fortunes);
        assertEquals(count, fortunes.size());
      }
    }
  }

  /**
   * Tests the {@link FortuneApiClient#getAllFortunes()} method without proper
   * authentication to ensure auth is working.
   *
   * @throws Exception There was an unexpected error
   */
  @Test
  public void testGetAllWithoutAuth() throws Exception {
    try (FortuneApi fortuneApi = new FortuneApi(apiConfig)) {
      // Run the server
      fortuneApi.run();

      // Get the randomly assigned address
      List<String> localAddresses = fortuneApi.getLocalAddresses();

      /*
       * This should only return one address based on the current logic, but
       * the test is written to work if multiple addresses are found.
       */
      for (String localAddress : localAddresses) {
        // Create a new client a scrambled bearer token
        FortuneApiClientConfig clientConfig = mock(FortuneApiClientConfig.class);
        when(clientConfig.baseUrl()).thenReturn(localAddress);
        when(clientConfig.sslCerts()).thenReturn(CERTS);

        FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(clientConfig);
        client.updateBearerToken(StringUtils.reverse(bearerToken));

        try {
          // Try to get the fortunes
          client.getAllFortunes();

          // If we get here, then bad auth was accepted
          fail();

        } catch (IOException e) {
          assertTrue(e.getMessage().contains("Unauthorized"));
        }
      }
    }
  }

  /**
   * Utility method to get the number of occurrences of a particular fortune in
   * the database.
   *
   * @param jdbi The database connection
   * @param fortune The fortune to check
   * @return The count
   */
  private int getCount(Jdbi jdbi, String fortune) {
    return jdbi.withHandle(handle ->
        handle.createQuery("SELECT COUNT(*) FROM fortune WHERE quote = :fortune")
            .bind("fortune", fortune)
            .mapTo(int.class)
            .first());
  }

  /**
   * Utility method to get the total number of fortunes in the database.
   *
   * @param jdbi The database connection
   * @return The total number of fortunes
   */
  private int getCount(Jdbi jdbi) {
    return jdbi.withHandle(handle ->
        handle.createQuery("SELECT COUNT(*) FROM fortune")
            .mapTo(int.class)
            .first());
  }
}
