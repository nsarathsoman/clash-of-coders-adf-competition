package hacker.service.http;

import hacker.JDKHttpClientHelper;

/**
 * Created by sarath on 7/10/16.
 */
public class HttpClientFactory {

    private static final HttpClient jdkClient = JDKHttpClientHelper.getInstance();
    private static final HttpClient okHttpClient = OkHttpClientHelper.getInstance();

    public static HttpClient getHttpClient(HttpClientType httpClientType){
        switch (httpClientType) {
            case C2: {
                return jdkClient;
            }
            case C1: {
                return okHttpClient;
            }
            default: return okHttpClient;
        }
    }

    public static enum HttpClientType {
        C1, C2
    }

}
