package net.shinton.fortune.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.shinton.fortune.client.cli.FortuneApiClientCli;
import net.shinton.fortune.factory.ObjectMapperFactory;
import net.shinton.fortune.model.RestResponseStatus;
import net.shinton.fortune.model.immutable.RestResponse;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import retrofit2.Call;
import retrofit2.Response;

/**
 * The Fortune API client class. This can be used as a library to make calls
 * from another Java program or invoked directly from the command line.
 */
public class FortuneApiClient {
  private static final ObjectMapper objectMapper = new ObjectMapperFactory().newMapper();

  private final FortuneApi api;
  private volatile String bearerToken;

  /**
   * <p>
   *   Creates a new client that communicates with the supplied Fortune API.
   * </p>
   * <p>
   *   <b>Note: </b>You will not be able to call any method that requires an
   *   authentication token until you call {@link #updateBearerToken(String)}.
   * </p>
   *
   * @param api The API to communicate with
   */
  public FortuneApiClient(FortuneApi api) {
    this(api, null);
  }

  /**
   * Creates a new client that communicates with the supplied Fortune API.
   *
   * @param api The API to communicate with
   * @param bearerToken The authentication token
   */
  public FortuneApiClient(FortuneApi api, String bearerToken) {
    this.api = api;
    this.bearerToken = bearerToken;
  }

  /**
   * Gets a fortune from the API.
   *
   * @return A fortune
   * @throws IOException There is a problem with the call
   */
  public String getFortune() throws IOException {
    Call<RestResponse<Map<String, String>>> call = api.getFortune();
    RestResponse<Map<String, String>> response = handleResponse(call.execute());
    Map<String, String> data = response.getData();
    return data.get("fortune");
  }

  /**
   * Gets all the fortunes in the database. This is an authenticated call and
   * requires a valid bearer token to be stored in the client.
   *
   * @return A list of fortunes
   * @throws IOException There is a problem with the call
   */
  public List<String> getAllFortunes() throws IOException {
    Call<RestResponse<List<String>>> call = api.getAllFortunes(bearerToken);
    RestResponse<List<String>> response = handleResponse(call.execute());
    return response.getData();
  }

  /**
   * Adds a fortune to the database. This is an authenticated call and
   * requires a valid bearer token to be stored in the client.
   *
   * @param fortune The fortune to add
   * @return <code>true</code> if added
   * @throws IOException There is a problem with the call
   */
  public boolean addFortune(String fortune) throws IOException {
    Call<RestResponse<Object>> call = api.addFortune(bearerToken, Map.of("fortune", fortune));
    RestResponse<Object> response = handleResponse(call.execute());
    return (response.getStatus() == RestResponseStatus.success);
  }

  /**
   * Updates or sets the bearer token used for authenticated calls.
   *
   * @param bearerToken The token
   */
  public void updateBearerToken(String bearerToken) {
    this.bearerToken = bearerToken;
  }

  /**
   * Utility method to do the repetitive task of parsing response objects.
   *
   * @param response The <code>retrofit</code> response
   * @return The encapsulated {@link RestResponse}
   * @param <T> The data type of the response
   * @throws IOException There is a problem obtaining the REST response
   */
  private <T> RestResponse<T> handleResponse(Response<RestResponse<T>> response) throws IOException {
    if (response.isSuccessful()) {
      return response.body();
    }

    try (ResponseBody errorBody = response.errorBody()) {
      String body = errorBody.string();

      if (StringUtils.isBlank(body)) {
        throw new IOException("An unknown error has occurred");
      }

      RestResponse<T> restResponse = objectMapper
          .readValue(body, new TypeReference<RestResponse<T>>() {});

      throw new IOException(String.format(Locale.ROOT, "%s (%d)",
          restResponse.getMessage(),
          restResponse.getCode()));
    }
  }

  /**
   * The main method used for command line invocation. For a list of commands
   * and options, run this class with the argument <code>--help</code>.
   *
   * @param args The program arguments
   */
  public static void main(String[] args) {
    System.exit(new CommandLine(new FortuneApiClientCli()).execute(args));
  }
}
