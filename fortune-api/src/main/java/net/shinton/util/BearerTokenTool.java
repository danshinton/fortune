package net.shinton.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import net.shinton.util.cli.BearerTokenToolCli;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * This tool is used to generate signing keys, generate bearer tokens, and
 * validate bearer tokens. For convenience, there is a CLI associated that
 * allows it to be invoked on the command line to facilitate automation and
 * manual testing.
 */
public class BearerTokenTool {
  private static final Logger log = LoggerFactory.getLogger(BearerTokenTool.class);

  private static final String ISSUER = "shinton.net";
  private static final String TOKEN_PREFIX = "Bearer ";
  private static final String PATH_DELIMITER = "/";

  private final String signingKey;
  private final String publicHost;

  /**
   * Creates a new <code>BearerTokenTool</code> that is ready to generate
   * and validate tokens.
   *
   * @param signingKey The JWT signing key
   * @param publicHost The public host of the API
   */
  public BearerTokenTool(String signingKey, String publicHost) {
    this.signingKey = signingKey;
    this.publicHost = publicHost;
  }

  /**
   * Validates the bearer token to ensure it is valid. This means that the token
   * was signed by the JWT signing key, was issued by <code>shinton.net</code>,
   * and the path encoded in the token matches the path of the API.
   *
   * @param token The token to validate
   * @param apiPath The API path
   * @return <code>true</code> if valid
   */
  public boolean validate(String token, String apiPath) {
    if (StringUtils.isBlank(token)) {
      log.warn("Empty bearer token provided");
      return false;
    }

    if (!token.startsWith(TOKEN_PREFIX)) {
      log.warn("Bearer token does not start with '{}': {}", TOKEN_PREFIX, token);
      return false;
    }

    try {
      Jws<Claims> jws = Jwts.parserBuilder()
          .setSigningKey(Base64.getDecoder().decode(signingKey))
          .build()
          .parseClaimsJws(token.substring(TOKEN_PREFIX.length()));

      Claims claims = jws.getBody();

      if (!ISSUER.equals(claims.getIssuer())) {
        log.warn("Invalid issuer for bearer token: {}", claims);
        return false;
      }

      if (!claims.getAudience().equalsIgnoreCase(joinPath(publicHost, apiPath))) {
        log.warn("Bearer token path mismatch: {}", claims);
        return false;
      }

      return true;

    } catch (MalformedJwtException | ExpiredJwtException e) {
      log.warn(e.getMessage());

    } catch (Exception e) {
      log.error("Unexpected error while validating token", e);
    }

    return false;
  }

  /**
   * Generates a new bearer token.
   *
   * @param expiresIn The number of <code>seconds</code> the token should stay alive
   * @param apiPath The API path for this token
   * @return The generated token
   */
  public String generate(int expiresIn, String apiPath) {
    Instant now = Instant.now();

    return TOKEN_PREFIX + Jwts.builder()
        .setIssuer(ISSUER)
        .setIssuedAt(Date.from(now))
        .setAudience(joinPath(publicHost, apiPath))
        .setExpiration(Date.from(now.plusSeconds(expiresIn)))
        .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(signingKey)))
        .compact();
  }

  /**
   * Joins two paths together in a way that prevents double delimitation.
   *
   * @param first The first half of the path
   * @param second The second half of the path
   * @return The joined path
   */
  private String joinPath(String first, String second) {
    if (first.endsWith(PATH_DELIMITER)) {
      first = first.substring(0, first.length() - 1);
    }

    if (second.startsWith(PATH_DELIMITER)) {
      second = second.substring(1);
    }

    return first + PATH_DELIMITER + second;
  }

  /**
   * This is a static method used to generate new signing keys of the correct
   * length that are accepted by the <code>BearerTokenTool</code>.
   *
   * @return The signing key
   */
  public static String newSigningKey() {
    return Base64.getEncoder().encodeToString(
        Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
  }

  /**
   * The main method used for command line invocation. For a list of commands
   * and options, run this class with the argument <code>--help</code>.
   *
   * @param args The program arguments
   */
  public static void main(String[] args) {
    System.exit(new CommandLine(new BearerTokenToolCli()).execute(args));
  }
}

