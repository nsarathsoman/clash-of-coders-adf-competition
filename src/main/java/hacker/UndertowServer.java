package hacker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import hacker.dao.CashFlowDAO;
import hacker.helper.ExecutorHelper;
import hacker.model.CashFlow;
import hacker.service.CashFlowService;
import hacker.service.http.HttpClientFactory;
import hacker.service.http.OkHttpClientHelper;
import hacker.service.parser.ParsingStrategy;
import hacker.util.Pair;
import io.undertow.Undertow;
import io.undertow.util.Headers;

/**
 * Created by sarath on 7/4/16.
 */
public class UndertowServer {

    public static String BANK_NAME_API = null;
    public static HikariDataSource HIKARI;
    private static ForkJoinPool forkJoinPool = new ForkJoinPool();
    public static ParsingStrategy.ParserType PARSER_TYPE;
    public static HttpClientFactory.HttpClientType HTTP_CLIENT_TYPE;

    public static void main(final String[] args) {
        System.out.println("Available Processors : " + Runtime.getRuntime().availableProcessors());
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(args[0]);
        config.setUsername(args[1]);
        config.setPassword(args[2]);
        config.setMinimumIdle(Integer.valueOf(args[7]));
        config.setMaximumPoolSize(Integer.valueOf(args[8]));
        ExecutorHelper.initialize(Integer.valueOf(args[9]));
        HIKARI = new HikariDataSource(config);
        BANK_NAME_API = args[3];
        PARSER_TYPE = ParsingStrategy.ParserType.valueOf(args[6]);
        HTTP_CLIENT_TYPE = HttpClientFactory.HttpClientType.valueOf(args[10]);

//        System.setProperty("http.keepAlive", "true");
//        System.setProperty("http.maxConnections", "50");

        OkHttpClientHelper.getInstance().getBankName("12348");

        Undertow server = Undertow.builder()
                .addHttpListener(Integer.parseInt(args[4]), args[5])
                .setHandler((exchange) -> {
                    final Deque<String> keys = exchange.getQueryParameters().get("key");
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    Pair<String, List<CashFlow>> resp = forkJoinPool.invoke(new RecursiveTask<Pair<String, List<CashFlow>>>() {
                        @Override
                        protected Pair<String, List<CashFlow>> compute() {
                            return CashFlowService.getInstance().process(Arrays.asList(keys.getFirst().split(",")));
                        }
                    });
                    exchange.getResponseSender().send(resp.getLeft());
                    ExecutorHelper.execute(() -> {
//                        OkHttpClientHelper.getInstance().postBankNameAndCashFlow(cashFlows);
                        CashFlowDAO.getInstance().updateBankNameAndCashFlow(resp.getRight());
                    });
                }).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ExecutorHelper.onJVMShutdown();
        }));

//        CashFlowDAO.getInstance().initCashFlowMap();

    }
}
