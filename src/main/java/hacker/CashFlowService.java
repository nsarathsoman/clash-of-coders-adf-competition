package hacker;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import okhttp3.OkHttpClient;

/**
 * Created by sarath on 7/4/16.
 */
public class CashFlowService {

    private static final CashFlowService cashFlowService = new CashFlowService();
    private final CashFlowDAO cashFlowDAO = CashFlowDAO.getInstance();
    private final List<ForkJoinTask<CashFlow>> forkJoinTasks = new ArrayList<>();
    private final List<CashFlow> cashFlowResults = new ArrayList<>();

    public static CashFlowService getInstance() {
        return cashFlowService;
    }

    private CashFlowService(){}

    public String process(List<String> keys) {
        LocalTime startTime = LocalTime.now();
        List<CashFlow> cashFlows = cashFlowDAO.getCashFlowEntities(keys);
        for (CashFlow cashFlow : cashFlows) {
            RecursiveTask<CashFlow> cashFlowRecursiveTask = new RecursiveTask<CashFlow>() {
                @Override
                protected CashFlow compute() {
                    return new StaxParser(cashFlow).parse();
                }
            };
            forkJoinTasks.add(cashFlowRecursiveTask);
            cashFlowRecursiveTask.fork();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = cashFlows.size() -1 ;
        for (int i = 0; i <= size; i++) {
//            CashFlow cashFlow = cashFlows.get(i);
            CashFlow cashFlow = forkJoinTasks.get(i).join();
            cashFlowResults.add(cashFlow);
            stringBuilder.append(cashFlow.toString());
            if (i != size) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        System.out.println("Process Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now())+ "ms");
//        new RecursiveAction() {
//
//            @Override
//            protected void compute() {
//                OkHttpClientHelper.getInstance().postBankNameAndCashFlow(cashFlows);
////                cashFlows.forEach(cashFlow -> CashFlowDAO.getInstance().updateBankNameAndCashFlow(cashFlows));
//            }
//        }.fork();
        ExecutorHelper.execute(() -> {
//            OkHttpClientHelper.getInstance().postBankNameAndCashFlow(cashFlows);
             CashFlowDAO.getInstance().updateBankNameAndCashFlow(cashFlowResults);
        });
        return stringBuilder.toString();
    }


    public String findBankName(String routingNumber, CashFlow cashFlow) {
//        Future<String> bankNameFuture =  ExecutorHelper.submit(() -> {
//        ExecutorHelper.execute(() -> {
            LocalTime startTime = LocalTime.now();
//            String bankName = JDKHttpClientHelper.getInstance().getBankName(routingNumber);
            String bankName = OkHttpClientHelper.getInstance().getBankName(routingNumber);
//            cashFlow.setBankName(bankName);
//            cashFlow.setRecievedbankName(true);
            System.out.println("Bank API Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
//            return bankName;
//            Thread.yield();
//        });
//        return bankNameFuture;
        return bankName;
    }
}
