package hacker.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import hacker.dao.CashFlowDAO;
import hacker.helper.ExecutorHelper;
import hacker.model.CashFlow;
import hacker.service.parser.DLXMLParser;
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
        final List<Future<CashFlow>> cashFlowFutures = new ArrayList<>();
        LocalTime startTime = LocalTime.now();
        System.out.println("Processing " + keys.toString());
        List<CashFlow> cashFlows = findCashFlows(keys);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(keys.size());
        cashFlows.forEach(cashFlow -> {
            Future<CashFlow> cashFlowFuture = ExecutorHelper.submit(() -> {
                startLatch.await();
                CashFlow cashFlowRs = DLXMLParser.getInstance().parse(cashFlow);
                stopLatch.countDown();
                return cashFlowRs;
            });
            cashFlowFutures.add(cashFlowFuture);
        });

        startLatch.countDown();
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final List<CashFlow> cashFlowResults = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = cashFlows.size() -1 ;
        for (int i = 0; i <= size; i++) {
            CashFlow cashFlow = null;
            try {
                cashFlow = cashFlowFutures.get(i).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

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
