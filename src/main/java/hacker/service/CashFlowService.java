package hacker.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import hacker.model.CashFlow;
import hacker.helper.ExecutorHelper;
import hacker.service.parser.DLXMLParser;
import hacker.dao.CashFlowDAO;

/**
 * Created by sarath on 7/4/16.
 */
public class CashFlowService {

    private static final CashFlowService cashFlowService = new CashFlowService();
    private final CashFlowDAO cashFlowDAO = CashFlowDAO.getInstance();
    private final List<ForkJoinTask<CashFlow>> forkJoinTasks = new ArrayList<>();

    public static CashFlowService getInstance() {
        return cashFlowService;
    }

    private CashFlowService(){}

    public String process(List<String> keys) {
        LocalTime startTime = LocalTime.now();
        System.out.println("Processing " + keys.toString());
        List<CashFlow> cashFlows = cashFlowDAO.getCashFlowEntities(keys);
        for (CashFlow cashFlow : cashFlows) {
            RecursiveTask<CashFlow> cashFlowRecursiveTask = new RecursiveTask<CashFlow>() {
                @Override
                protected CashFlow compute() {
                    return DLXMLParser.getInstance().parse(cashFlow);
                }
            };
            forkJoinTasks.add(cashFlowRecursiveTask);
            cashFlowRecursiveTask.fork();
        }
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

        ExecutorHelper.execute(() -> {
//            OkHttpClientHelper.getInstance().postBankNameAndCashFlow(cashFlows);
             CashFlowDAO.getInstance().updateBankNameAndCashFlow(cashFlowResults);
        });
        return stringBuilder.toString();
    }

}
