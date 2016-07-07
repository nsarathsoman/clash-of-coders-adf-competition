package hacker;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    public String process(List<String> keys) {
        LocalTime startTime = LocalTime.now();
        List<CashFlow> cashFlows = cashFlowDAO.getCashFlowEntities(keys);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(cashFlows.size());
//        List<Future<CashFlow>> futures = new ArrayList<>();
        for (CashFlow cashFlow : cashFlows) {
            ExecutorHelper.execute(() -> {
//                try {
//                    startSignal.await();
//                    new RawDLXMLFileProcessor(cashFlow).parse();
//                new SaxParser(cashFlow).parse();
                new StaxParser(cashFlow).parse();
//                    doneSignal.countDown();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            });
        }
//        executorService.shutdown();

//        startSignal.countDown();
//        try {
//            doneSignal.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        futures.forEach(future -> {
//            try {
//                future.get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        });

        LocalTime bankNameRecievedTIme = LocalTime.now();
        while(!cashFlows.stream().allMatch(cashFlow -> cashFlow.isRecievedbankName() )){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("BankNameRecieved Check Time : " + ChronoUnit.MILLIS.between(bankNameRecievedTIme, LocalTime.now())+ "ms");

        LocalTime cashFlowTime = LocalTime.now();
        while(!cashFlows.stream().allMatch(cashFlow -> cashFlow.isCashFlowAdded() )){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("CashFlow Check Time : " + ChronoUnit.MILLIS.between(cashFlowTime, LocalTime.now())+ "ms");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = cashFlows.size() -1 ;
        for (int i = 0; i <= size; i++) {
            CashFlow cashFlow = cashFlows.get(i);
            stringBuilder.append(cashFlow.toString());
            if (i != size) {
                stringBuilder.append(",");
            }
        }
        stringBuilder.append("]");
        System.out.println("Process Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now())+ "ms");
        ExecutorHelper.execute(() -> {
            cashFlows.forEach(cashFlow -> CashFlowDAO.getInstance().updateBankNameAndCashFlow(cashFlows));
        });
        return stringBuilder.toString();
    }


    public Future<String> findBankName(String routingNumber, CashFlow cashFlow) {
//        Future<String> bankNameFuture =  ExecutorHelper.submit(() -> {
        ExecutorHelper.execute(() -> {
            LocalTime startTime = LocalTime.now();
//            String bankName = JDKHttpClientHelper.getInstance().getBankName(routingNumber);
            String bankName = OkHttpClientHelper.getInstance().getBankName(routingNumber);
            cashFlow.setBankName(bankName);
            cashFlow.setRecievedbankName(true);
            System.out.println("Bank API Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
//            return bankName;
//            Thread.yield();
        });
//        return bankNameFuture;
        return null;
    }
}
