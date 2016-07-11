package hacker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Arrays;
import java.util.Deque;

import io.undertow.Undertow;
import io.undertow.util.Headers;

/**
 * Created by sarath on 7/4/16.
 */
public class UndertowServer {

    private static HikariConfig config;
    public static HikariDataSource hikariDataSource;

    public static void main(final String[] args) {
        config = new HikariConfig();
        config.setJdbcUrl(args[0]);
        config.setUsername(args[1]);
        config.setPassword(args[2]);
        config.setMinimumIdle(100);
        config.setMaximumPoolSize(100);
        hikariDataSource = new HikariDataSource(config);

//        System.setProperty("http.keepAlive", "true");
//        System.setProperty("http.maxConnections", "50");

//        ExecutorHelper.execute(() -> {
//            JDKHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12346").build());
//            JDKHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12347").build());
//            JDKHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12348").build());
//            JDKHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12349").build());

//            OkHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12345").build());
//            OkHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12346").build());
//            OkHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12347").build());
//            OkHttpClientHelper.getInstance().getBankName(CashFlow.CashFlowBuilder.builder().withRoutingNumber("12348").build());

//        });

        Undertow server = Undertow.builder()
                .addHttpListener(8888, args[3])
                .setHandler((exchange) -> {
                    Deque<String> keys = exchange.getQueryParameters().get("key");
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    String resp = CashFlowService.getInstance().process(Arrays.asList(keys.getFirst().split(",")));
                    exchange.getResponseSender().send(resp);
                }).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ExecutorHelper.onJVMShutDown();
        }));
    }
}
