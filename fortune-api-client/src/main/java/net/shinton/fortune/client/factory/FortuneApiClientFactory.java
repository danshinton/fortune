package net.shinton.fortune.client.factory;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import net.shinton.fortune.client.FortuneApi;
import net.shinton.fortune.client.FortuneApiClient;
import net.shinton.fortune.client.FortuneApiClientConfig;
import net.shinton.fortune.factory.KeystoreFactory;
import net.shinton.fortune.factory.ObjectMapperFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Factory class to create a properly configured {@link FortuneApiClient}.
 */
public class FortuneApiClientFactory {
  /**
   * Create a new {@link FortuneApiClient} based on the passed configuration.
   *
   * @param config The configuration
   * @return A new Fortune API client
   * @throws NoSuchAlgorithmException There is a problem initializing the trust
   *         manager used for SSL
   * @throws KeyStoreException There is a problem initializing the keystore
   * @throws KeyManagementException SSL cannot be initialized
   */
  public FortuneApiClient newFortuneApiClient(FortuneApiClientConfig config) throws
      NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

    // Initialize the HTTP Client
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS)
        .readTimeout(config.readTimeout(), TimeUnit.MILLISECONDS)
        .writeTimeout(config.writeTimeout(), TimeUnit.MILLISECONDS);

    // Configure any self-signed certificates
    if (config.sslCerts() != null) {
      KeyStore keyStore = new KeystoreFactory().newClientKeystore(config.sslCerts());

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);

      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      X509TrustManager x509TrustManager = (X509TrustManager) trustManagers[0];

      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      builder.sslSocketFactory(sslSocketFactory, x509TrustManager);
    }

    // Initialize Retrofit
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(config.baseUrl())
        .client(builder.build())
        .addConverterFactory(JacksonConverterFactory.create(
            new ObjectMapperFactory().newMapper()))
        .build();

    // Create the client interface
    return new FortuneApiClient(retrofit.create(FortuneApi.class));
  }
}
