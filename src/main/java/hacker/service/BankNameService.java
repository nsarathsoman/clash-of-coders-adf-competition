package hacker.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;

import hacker.helper.ExecutorHelper;
import hacker.model.CashFlow;
import hacker.service.http.OkHttpClientHelper;

/**
 * Created by sarath on 7/9/16.
 */
public class BankNameService {

    private static final BankNameService bankNameService = new BankNameService();

    private BankNameService(){}

    public static BankNameService getInstance() {
        return bankNameService;
    }

    public Future<String> findBankName(String routingNumber) {
        return ExecutorHelper.submit(() -> {
            LocalTime startTime = LocalTime.now();
            String bankName = OkHttpClientHelper.getInstance().getBankName(routingNumber);
//            Thread.sleep(1);
//            String bankName = "DUMMY";
            System.out.println("Bank API Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
            return bankName;
        });
    }
}
