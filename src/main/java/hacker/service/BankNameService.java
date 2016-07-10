package hacker.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import hacker.UndertowServer;
import hacker.service.http.HttpClientFactory;
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

    public String findBankName(String routingNumber) {
        LocalTime startTime = LocalTime.now();
        String bankName = HttpClientFactory.getHttpClient(UndertowServer.HTTP_CLIENT_TYPE).getBankName(routingNumber);
        System.out.println("Bank API Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
        return bankName;
    }
}
