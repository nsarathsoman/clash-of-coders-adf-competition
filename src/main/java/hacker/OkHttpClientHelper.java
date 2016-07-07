package hacker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sarath on 7/5/16.
 */
public class OkHttpClientHelper {

    static {
        okClient = getUnsafeOkHttpClient();
    }

    private static final OkHttpClientHelper httpClient = new OkHttpClientHelper();
    private static OkHttpClient okClient;

    private OkHttpClientHelper() {
    }

    public static OkHttpClientHelper getInstance() {
        return httpClient;
    }

    public String getBankName(String routingNumber) {
        Request request = new Request.Builder()
                .url("https://www.dev-ui1.adfdata.net/hacker/bank/" + routingNumber)
                .build();

        InputStream inputStream = null;
        try (Response response = okClient.newCall(request).execute()) {
            inputStream = response.body().byteStream();
            ObjectMapper objectMapper = new ObjectMapper();
            BankAPIResponse bankAPIResponse = objectMapper.readValue(inputStream, BankAPIResponse.class);
//            System.out.println("Bank Name recieved :" + cashFlow.getBankName());
            inputStream.close();
            return bankAPIResponse.getBankName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

        private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.connectionPool(new ConnectionPool(20, 10L, TimeUnit.MINUTES)).build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
