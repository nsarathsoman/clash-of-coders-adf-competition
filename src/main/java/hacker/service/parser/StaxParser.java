package hacker.service.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.RecursiveTask;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import hacker.model.CashFlow;
import hacker.service.BankNameService;


public class StaxParser implements Parser{

    private static final StaxParser STAX_PARSER = new StaxParser();

    private StaxParser() {}

    public static StaxParser getInstance() {
        return STAX_PARSER;
    }

    public CashFlow parse(final CashFlow cashFlow) {
        final String filePath = cashFlow.getDlFileName();;
        double cashFlowAmount = 0f;
        String routingNumber = null;
        RecursiveTask<String> bankNameAction = null;
        LocalTime startTime = LocalTime.now();
        boolean amount = false;
        boolean routingNumberEntered = false;
        boolean routingNumberRead = false;
        boolean transactionSummaryRead = false;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileReader(filePath));
            int event = xmlStreamReader.getEventType();
            while (true) {
                switch (event){
                    case XMLStreamConstants.START_ELEMENT: {
                        if("TransactionSummary4".equals(xmlStreamReader.getLocalName())){
                            transactionSummaryRead = true;
                        }
                        if(transactionSummaryRead && "Amount".equals(xmlStreamReader.getLocalName())){
                            amount = true;
                        } else if(!routingNumberRead && "RoutingNumberEntered".equals(xmlStreamReader.getLocalName())) {
                            routingNumberEntered = true;
                        }
                        break;
                    }

                    case XMLStreamConstants.CHARACTERS: {
                        if (amount) {
                            cashFlowAmount += Double.valueOf(xmlStreamReader.getText());
                            amount = false;
                            transactionSummaryRead = false;
                        }
                        if (routingNumberEntered) {
                            routingNumber = xmlStreamReader.getText();
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
                    .withCashFlow((int)Math.ceil(cashFlowAmount))
                    .withBankName(BankNameService.getInstance().findBankName(routingNumber))
                    .done();
            System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
            return cashFlowRes;
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void main(String[] args) {
//        LocalTime startTime = LocalTime.now();
//        CashFlow cashFlow = CashFlow.CashFlowBuilder.build().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").done();
//        new StaxParser(cashFlow).parse();
//        System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
//    }


    public int round(float num) {

        if (num > 0) {
            return (int) (num + 0.5d);
        } else {
            return (int) (num - 0.5d);
        }
    }
}
