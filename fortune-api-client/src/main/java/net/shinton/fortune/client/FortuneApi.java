package net.shinton.fortune.client;

import java.util.List;
import java.util.Map;
import net.shinton.fortune.model.immutable.RestResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * This is an interface annotated with <code>Retrofit</code> to generate code
 * needed to communicate with the Fortune API.
 */
public interface FortuneApi {
  /**
   * Get a single fortune.
   *
   * @return The response object containing a fortune
   */
  @GET("/api/v1/fortune")
  Call<RestResponse<Map<String, String>>> getFortune();

  /**
   * Add a fortune to the database. The body must contain a key of "fortune" with
   * the value being the fortune to add.
   *
   * @param bearerToken The token needed for authentication
   * @param body A map of parameters for the call
   * @return The response object
   */
  @POST("/api/v1/fortune")
  Call<RestResponse<Object>> addFortune(@Header("Authorization") String bearerToken, @Body Map<String, String> body);

  /**
   * Get all fortunes.
   *
   * @param bearerToken The token needed for authentication
   * @return The response object containing a list of fortunes
   */
  @GET("/api/v1/fortune/all")
  Call<RestResponse<List<String>>> getAllFortunes(@Header("Authorization") String bearerToken);
}
