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
import hacker.service.parser.SaxParser;
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
        final List<Future<Pair<CashFlow, Future<String>>>> cashFlowFutures = new ArrayList<>();
        LocalTime startTime = LocalTime.now();
        System.out.println("Processing " + keys.toString());
        List<CashFlow> cashFlows = findCashFlows(keys);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(keys.size());
        cashFlows.forEach(cashFlow -> {
            Future<Pair<CashFlow, Future<String>>> cashFlowFuture = ExecutorHelper.submit(() -> {
                startLatch.await();
                Pair<CashFlow, Future<String>> resPair = DLXMLParser.getInstance().parse(cashFlow);
//                CashFlow resPair = SaxParser.getInstance().parse(cashFlow);
                stopLatch.countDown();
                return resPair;
            });
            cashFlowFutures.add(cashFlowFuture);
        });

        final List<CashFlow> cashFlowResults = new ArrayList<>();
//        cashFlows.parallelStream().forEach(cashFlow -> {
//            CashFlow cashFlowRs = DLXMLParser.getInstance().parse(cashFlow);
////            CashFlow cashFlowRs = SaxParser.getInstance().parse(cashFlow);
//            cashFlowResults.add(cashFlowRs);
//        });

        startLatch.countDown();
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cashFlowFutures.stream().allMatch(pairFuture -> {
            try {
                return pairFuture.get().getRight().isDone();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return false;
        });
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = cashFlows.size() -1 ;
        for (int i = 0; i <= size; i++) {
            CashFlow cashFlow = null;
            try {
                Pair<CashFlow, Future<String>> resPair = cashFlowFutures.get(i).get();
                cashFlow = resPair.getLeft();
                cashFlow.setBankName(resPair.getRight().get());
//                cashFlow = cashFlowResults.get(i);
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
