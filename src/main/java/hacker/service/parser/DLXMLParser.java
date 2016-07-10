package hacker.service.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import hacker.model.CashFlow;
import hacker.service.BankNameService;
import hacker.util.Pair;


public class DLXMLParser {

    private static final DLXMLParser DLXML_PARSER = new DLXMLParser();

    private DLXMLParser() {}

    public static DLXMLParser getInstance() {
        return DLXML_PARSER;
    }

    public Pair<CashFlow, Future<String>> parse(final CashFlow cashFlow) {
        final String filePath = cashFlow.getDlFileName();;
        float cashFlowAmount = 0f;
        String routingNumber = null;
        LocalTime startTime = LocalTime.now();
        boolean amount = false;
        boolean routingNumberEntered = false;
        boolean routingNumberRead = false;
        Future<String> bankNameFuture = null;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileReader(filePath));
            int event = xmlStreamReader.getEventType();
            while (true) {
                switch (event){
                    case XMLStreamConstants.START_ELEMENT: {
                        if("Amount".equals(xmlStreamReader.getLocalName())){
                            amount = true;
                        } else if(!routingNumberRead && "RoutingNumberEntered".equals(xmlStreamReader.getLocalName())) {
                            routingNumberEntered = true;
                        }
                        break;
                    }

                    case XMLStreamConstants.CHARACTERS: {
                        if (amount) {
                            cashFlowAmount += Float.valueOf(xmlStreamReader.getText());
                            amount = false;
                        }
                        if (routingNumberEntered) {
                            routingNumber = xmlStreamReader.getText();
                            bankNameFuture = BankNameService.getInstance().findBankName(routingNumber);
                            routingNumberEntered = false;
                            routingNumberRead = true;
                        }
                        break;
                    }
                }

                if (!xmlStreamReader.hasNext()) {
                    break;
                }

                event = xmlStreamReader.next();
            }

//            final String finalRoutingNumber = routingNumber;
//            bankNameAction = new RecursiveTask<String>() {
//                @Override
//                protected String compute() {
//                    return BankNameService.getInstance().findBankName(finalRoutingNumber);
//                }
//            };
//            bankNameAction.fork();
            CashFlow cashFlowRes = CashFlow.CashFlowBuilder.build()
                    .withKey(cashFlow.getKey())
                    .withCashFlow((int) (cashFlowAmount + 0.5f))
//                    .withBankName(/*BankNameService.getInstance().findBankName(routingNumber)*/"DUmmy")
                    .done();
            System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
            return new Pair<CashFlow, Future<String>>(cashFlowRes, bankNameFuture);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        LocalTime total = LocalTime.now();
        for(int i=0; i<10; i++) {
            LocalTime startTime = LocalTime.now();
            CashFlow cashFlow = CashFlow.CashFlowBuilder.build().withDLFileName("/home/sarath/Downloads/DLxml_3_amount check.xml").done();
            new DLXMLParser().parse(cashFlow);
            System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
        }
        System.out.println("Total Time Taken : " + ChronoUnit.MILLIS.between(total, LocalTime.now()));
    }


    public int round(float num) {

        if (num > 0) {
            return (int) (num + 0.5d);
        } else {
            return (int) (num - 0.5d);
        }
    }
}
