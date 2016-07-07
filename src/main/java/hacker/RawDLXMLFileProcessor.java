package hacker;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.concurrent.Future;

/**
 * Created by sarath on 7/4/16.
 */
public class RawDLXMLFileProcessor {

    private final String filePath;
    private final CashFlow cashFlow;
    private float cashFlowAmount = 0f;

    public RawDLXMLFileProcessor(CashFlow cashFlow) {
        this.cashFlow = cashFlow;
        this.filePath = cashFlow.getDlFileName();
    }

    public void parse() {
        LocalTime startTime = LocalTime.now();
        try(FileInputStream fis = new FileInputStream(filePath);) {
            while(fis.available() > 0) {
                char ch = (char) fis.read();
//                System.out.print(ch);
                switch(ch){
                    case '<' : {
                        checkElement(fis);
                        break;
                    }
                }
            }
            cashFlow.setCashFlow(cashFlowAmount);
            cashFlow.setCashFlowAdded(true);
//            Thread.yield();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }

    private void checkElement(FileInputStream fis) throws IOException {
        char ch = (char) fis.read();
        switch (ch) {
            case 'A':
                byte[] amountElem = new byte[6];//mount>
                fis.read(amountElem);
                if(new String(amountElem).equals("mount>")){
                    readAmount(fis);
                }
                break;
            case 'R':{
                byte[] routingNumberEnteredElem = new byte[20];//outingNumberEntered>
                fis.read(routingNumberEnteredElem);
                if(new String(routingNumberEnteredElem).equals("outingNumberEntered>")){
                    readRoutingNumberEntered(fis);
                }
                break;
            }
        }
    }

    private void readRoutingNumberEntered(FileInputStream fis) throws IOException {
        char ch = (char) fis.read();
        StringBuilder sb = new StringBuilder();
        while ('<' != ch) {
            sb.append(ch);
            ch = (char) fis.read();
        }
        CashFlowService.getInstance().findBankName(sb.toString(), cashFlow);
//        System.out.println("RoutingNumberEnteredListener : " + sb.toString());
    }

    private void readAmount(FileInputStream fis) throws IOException {
        char ch = (char) fis.read();
        StringBuilder sb = new StringBuilder();
        while ('<' != ch) {
            sb.append(ch);
            ch = (char) fis.read();
        }
        try {
            float amount = Float.valueOf(sb.toString());
            cashFlowAmount += amount;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
//        amountEventListener.recieve(sb.toString());
//        System.out.println("Amount : " + sb.toString());
    }

    public static void main(String[] args) {
        for(int i = 0; i< 5; i++) {
            LocalTime startTime = LocalTime.now();
            CashFlow cashFlow = CashFlow.CashFlowBuilder.builder().withDLFileName("/home/sarath/Downloads/DLxml_3_amount check.xml").build();
            new RawDLXMLFileProcessor(cashFlow).parse();
            System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
        }
    }
}
