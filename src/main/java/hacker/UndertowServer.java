package hacker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import hacker.service.CashFlowService;
import hacker.service.http.OkHttpClientHelper;
import io.undertow.Undertow;
import io.undertow.util.Headers;

/**
 * Created by sarath on 7/4/16.
 */
public class UndertowServer {

    public static String BANK_NAME_API = null;
    public static HikariDataSource HIKARI;
    private static ForkJoinPool forkJoinPool = new ForkJoinPool();

    public static void main(final String[] args) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(args[0]);
        config.setUsername(args[1]);
        config.setPassword(args[2]);
        config.setMinimumIdle(100);
        config.setMaximumPoolSize(100);
        HIKARI = new HikariDataSource(config);
        BANK_NAME_API = args[3];

//        System.setProperty("http.keepAlive", "true");
//        System.setProperty("http.maxConnections", "50");

        OkHttpClientHelper.getInstance().getBankName("12348");
        Undertow server = Undertow.builder()
                .addHttpListener(Integer.parseInt(args[4]), args[5])
                .setHandler((exchange) -> {
                    final Deque<String> keys = exchange.getQueryParameters().get("key");
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    String resp = forkJoinPool.invoke(new RecursiveTask<String>() {
                        @Override
                        protected String compute() {
                            return CashFlowService.getInstance().process(Arrays.asList(keys.getFirst().split(",")));
                        }
                    });
                    exchange.getResponseSender().send(resp);
                }).build();
        server.start();

    }
}
