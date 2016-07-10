package hacker.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import hacker.UndertowServer;
import hacker.dao.CashFlowDAO;
import hacker.model.CashFlow;
import hacker.service.parser.ParsingStrategy;
import hacker.util.Pair;

/**
 * Created by sarath on 7/4/16.
 */
public class CashFlowService {

    private static final CashFlowService cashFlowService = new CashFlowService();
    private final CashFlowDAO cashFlowDAO = CashFlowDAO.getInstance();

    public static CashFlowService getInstance() {
        return cashFlowService;
    }

    private CashFlowService(){}

    public Pair<String, List<CashFlow>> process(List<String> keys) {
        final List<ForkJoinTask<CashFlow>> forkJoinTasks = new ArrayList<>();
        LocalTime startTime = LocalTime.now();
        System.out.println("Processing " + keys.toString());
        List<CashFlow> cashFlows = findCashFlows(keys);
        cashFlows.forEach(cashFlow -> {
            RecursiveTask<CashFlow> cashFlowRecursiveTask = new RecursiveTask<CashFlow>() {
                @Override
                protected CashFlow compute() {
                    return ParsingStrategy.findParser(UndertowServer.PARSER_TYPE).parse(cashFlow);
                }
            };
            forkJoinTasks.add(cashFlowRecursiveTask);
            cashFlowRecursiveTask.fork();
        });

        final List<CashFlow> cashFlowResults = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = cashFlows.size() -1 ;
        for (int i = 0; i <= size; i++) {
            CashFlow cashFlow = forkJoinTasks.get(i).join();

            cashFlowResults.add(cashFlow);
            stringBuilder.append(cashFlow.toString());
            if (i != size) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        System.out.println("Process Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now())+ "ms");

        return new Pair<>(stringBuilder.toString(), cashFlowResults);
    }

    private List<CashFlow> findCashFlows(List<String> keys) {
//        List<CashFlow> cashFlows = new ArrayList<>();
//        Map<String, CashFlow> cashFlowMap = cashFlowDAO.getCashFlowMap();
//        keys.forEach((key) -> cashFlows.add(cashFlowMap.get(key)));
//        return cashFlows;
        return cashFlowDAO.getCashFlows(keys);
    }

}
