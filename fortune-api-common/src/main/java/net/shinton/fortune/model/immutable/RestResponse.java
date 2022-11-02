package net.shinton.fortune.model.immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;
import net.shinton.fortune.model.RestResponseStatus;
import org.immutables.value.Value;

/**
 * Standard REST response POJO used to deserialize API responses.
 *
 * @param <T> The payload of the response
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRestResponse.class)
@JsonDeserialize(as = ImmutableRestResponse.class)
public interface RestResponse<T> {
  /**
   * The status of the call; either success or error.
   *
   * @return The status
   */
  RestResponseStatus getStatus();

  /**
   * The HTTP error code of the response.
   *
   * @return The error code
   */
  int getCode();

  /**
   * The message that provides additional details about the status
   *
   * @return The message
   */
  @Nullable
  String getMessage();

  /**
   * The data payload of the response.
   *
   * @return The data
   */
  @Nullable
  T getData();
}
