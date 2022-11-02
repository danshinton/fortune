package net.shinton.util.cli;

import java.util.concurrent.Callable;
import net.shinton.annotation.SuppressForbidden;
import net.shinton.util.BearerTokenTool;
import picocli.CommandLine;

/**
 * This is a class that is annotated to trigger <code>picocli</code> to
 * generate a command line interface for the {@link BearerTokenTool}.
 */
@SuppressWarnings({"PMD.SystemPrintln"})
@CommandLine.Command(name = "BearerTokenTool",
    mixinStandardHelpOptions = true,
    version = "BearerTokenTool 1.0",
    description = "Utility for working with bearer tokens",
    subcommands = {BearerTokenToolCli.Token.class, BearerTokenToolCli.Key.class})
public class BearerTokenToolCli {
  @SuppressForbidden(reason = "System#out")
  @CommandLine.Command(name = "token", description = "Generate a bearer token")
  static class Token implements Callable<Integer> {
    @CommandLine.Option(names = {"-k", "--signing-key"},
        description = "A Base64 encoded HS512 signing key",
        required = true)
    private String signingKey;

    @CommandLine.Option(names = {"-p", "--public-host"},
        description = "The public hostname of the api (e.g. fortune.shinton.net)",
        required = true)
    private String publicHost;

    @CommandLine.Option(names = {"-e", "--expires-in"},
        description = "The number of seconds the token should be valid. Defaults to 86400 (1 day).",
        defaultValue = "86400")
    private int expiresIn;

    @CommandLine.Option(names = {"-a", "--api-path"},
        description = "The API path for the token. Defaults to \"/api/v1/fortune\".",
        defaultValue = "/api/v1/fortune")
    private String apiPath;

    @Override
    public Integer call() throws Exception {
      System.out.println(new BearerTokenTool(signingKey, publicHost).generate(expiresIn, apiPath));
      return 0;
    }
  }

  @SuppressForbidden(reason = "System#out")
  @CommandLine.Command(name = "key", description = "Generate a signing key")
  static class Key implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
      System.out.println(BearerTokenTool.newSigningKey());
      return 0;
    }
  }
}
