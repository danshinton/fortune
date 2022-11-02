package net.shinton.fortune.client.cli;

import java.util.concurrent.Callable;
import net.shinton.annotation.SuppressForbidden;
import net.shinton.fortune.client.FortuneApiClient;
import net.shinton.fortune.client.FortuneApiClientConfig;
import net.shinton.fortune.client.ImmutableFortuneApiClientConfig;
import net.shinton.fortune.client.factory.FortuneApiClientFactory;
import picocli.CommandLine;

/**
 * This is a class that is annotated to trigger <code>picocli</code> to
 * generate a command line interface for the {@link FortuneApiClient}.
 */
@SuppressWarnings({"PMD.SystemPrintln"})
@CommandLine.Command(name = "FortuneApiClient",
    mixinStandardHelpOptions = true,
    version = "FortuneApiClient 1.0",
    description = "Utility for querying fortune api",
    subcommands = {FortuneApiClientCli.Get.class, FortuneApiClientCli.GetAll.class, FortuneApiClientCli.Add.class})
public class FortuneApiClientCli {
  @SuppressForbidden(reason = "System#out")
  @CommandLine.Command(name = "get", description = "Get a new fortune")
  static class Get implements Callable<Integer> {
    @CommandLine.Option(names = {"-u", "--url"},
        description = "The base url of the Fortune API. For example: http://fortune.shinton.net.",
        required = true)
    private String url;

    @Override
    public Integer call() throws Exception {
      FortuneApiClientConfig config = ImmutableFortuneApiClientConfig.builder()
          .baseUrl(url)
          .connectTimeout(10_000L)
          .readTimeout(10_000L)
          .writeTimeout(10_000L)
          .build();

      FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(config);

      System.out.println(client.getFortune());
      return 0;
    }
  }

  @SuppressForbidden(reason = "System#out")
  @CommandLine.Command(name = "get-all", description = "Get all fortunes")
  static class GetAll implements Callable<Integer> {
    @CommandLine.Option(names = {"-u", "--url"},
        description = "The base url of the Fortune API. For example: http://fortune.shinton.net.",
        required = true)
    private String url;

    @CommandLine.Option(names = {"-a", "--auth-token"},
        description = "The bearer token to use to authenticate this call.",
        required = true)
    private String bearerToken;

    @Override
    public Integer call() throws Exception {
      FortuneApiClientConfig config = ImmutableFortuneApiClientConfig.builder()
          .baseUrl(url)
          .connectTimeout(10_000L)
          .readTimeout(10_000L)
          .writeTimeout(10_000L)
          .build();

      FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(config);
      client.updateBearerToken(bearerToken);

      for (String fortune : client.getAllFortunes()) {
        System.out.println(fortune);
      }

      return 0;
    }
  }

  @SuppressForbidden(reason = "System#out")
  @CommandLine.Command(name = "add", description = "Add a new fortune to the list")
  static class Add implements Callable<Integer> {
    @CommandLine.Option(names = {"-u", "--url"},
        description = "The base url of the Fortune API. For example: http://fortune.shinton.net.",
        required = true)
    private String url;

    @CommandLine.Option(names = {"-a", "--auth-token"},
        description = "The bearer token to use to authenticate this call.",
        required = true)
    private String bearerToken;

    @CommandLine.Option(names = {"-f", "--fortune"},
        description = "The fortune to add.",
        required = true)
    private String fortune;

    @Override
    public Integer call() throws Exception {
      FortuneApiClientConfig config = ImmutableFortuneApiClientConfig.builder()
          .baseUrl(url)
          .connectTimeout(10_000L)
          .readTimeout(10_000L)
          .writeTimeout(10_000L)
          .build();

      FortuneApiClient client = new FortuneApiClientFactory().newFortuneApiClient(config);
      client.updateBearerToken(bearerToken);

      if (client.addFortune(fortune)) {
        System.out.println("Fortune successfully added");
      } else {
        System.out.println("Unable to add fortune");
      }

      return 0;
    }
  }
}
