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
public class JDKHttpClientHelper {

    static {
        disableSslVerification();
    }

    private static final JDKHttpClientHelper httpClient = new JDKHttpClientHelper();

    private JDKHttpClientHelper() {
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static JDKHttpClientHelper getInstance() {
        return httpClient;
    }

    public String getBankName(String routingNumber) {
//        System.out.println("Finding BankName");
        InputStream inputStream = null;
        HttpsURLConnection httpsURLConnection = null;
        try {
            URL url = new URL("https://www.dev-ui1.adfdata.net/hacker/bank/" + routingNumber);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.setRequestProperty("Keep-Alive", "timeout=6000");
//            httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
            inputStream = httpsURLConnection.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            BankAPIResponse bankAPIResponse = objectMapper.readValue(inputStream, BankAPIResponse.class);
//            StringBuilder bankNameBuilder = new StringBuilder();
////            System.out.println();
//            while (inputStream.available() > 0) {
//                char ch = (char) inputStream.read();
////                System.out.print(ch);
//                switch (ch) {
//                    case '"': {
//                        ch = (char) inputStream.read();
//                        switch (ch) {
//                            case 'b': {
//                                byte[] bankNameField = new byte[8]; //ankName"
//                                inputStream.read(bankNameField);
////                                System.out.print(new String(bankNameField));
//                                if (new String(bankNameField).equals("ankName\"")) {
//                                    do {
//                                        ch = (char) inputStream.read();
////                                        System.out.print(ch);
//                                    } while (':' == ch);
//                                    do {
//                                        ch = (char) inputStream.read();
////                                        System.out.println(ch);
//                                    } while ('"' == ch);
//                                    while ('"' != ch) {
//                                        bankNameBuilder.append(ch);
//                                        ch = (char) inputStream.read();
////                                        System.out.print(ch);
//                                    }
//                                }
//                            }
//                        }
//                        break;
//                    }
//
//                }
//            }
////            System.out.println();
//            bankNameBuilder.toString();
            inputStream.close();
            return bankAPIResponse.getBankName();
//            httpsURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(null != httpsURLConnection){
                httpsURLConnection.disconnect();
            }
            e.printStackTrace();
        }
        return null;
    }

}
