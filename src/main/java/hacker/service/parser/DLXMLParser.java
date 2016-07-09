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


public class DLXMLParser {

    private static final DLXMLParser DLXML_PARSER = new DLXMLParser();

    private DLXMLParser() {}

    public static DLXMLParser getInstance() {
        return DLXML_PARSER;
    }

    public CashFlow parse(final CashFlow cashFlow) {
        final String filePath = cashFlow.getDlFileName();;
        float cashFlowAmount = 0f;
        RecursiveTask<String> bankNameAction = null;
        LocalTime startTime = LocalTime.now();
        boolean amount = false;
        boolean routingNumberEntered = false;
        boolean routingNumberRead = false;
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
                            final String routingNumber = xmlStreamReader.getText();
                            bankNameAction = new RecursiveTask<String>() {
                                @Override
                                protected String compute() {
                                    return BankNameService.getInstance().findBankName(routingNumber);
                                }
                            };
                            bankNameAction.fork();
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

            return CashFlow.CashFlowBuilder.build()
                    .withKey(cashFlow.getKey())
                    .withCashFlow((int) (cashFlowAmount + 0.5f))
                    .withBankName(bankNameAction.join())
                    .done();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
        return null;
    }

//    public static void main(String[] args) {
//        LocalTime startTime = LocalTime.now();
//        CashFlow cashFlow = CashFlow.CashFlowBuilder.build().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").done();
//        new DLXMLParser(cashFlow).parse();
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
