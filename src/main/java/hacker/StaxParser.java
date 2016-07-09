package hacker;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.RecursiveTask;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class StaxParser {

    private final String filePath;
    private final CashFlow cashFlow;
    private float cashFlowAmount = 0f;
    private RecursiveTask<String> bankNameAction;

    public StaxParser(CashFlow cashFlow) {
        this.cashFlow = cashFlow;
        this.filePath = cashFlow.getDlFileName();
    }

    public CashFlow parse() {
        LocalTime startTime = LocalTime.now();
        boolean amount = false;
        boolean routingNumberEntered = false;
        boolean routingNumberRead = false;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileReader(cashFlow.getDlFileName()));
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
//                                    cashFlow.setBankName("sas");
                                    return CashFlowService.getInstance().findBankName(routingNumber, cashFlow);
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

//            cashFlow.setCashFlow((int) (cashFlowAmount + 0.5f));
//            cashFlow.setCashFlowAdded(true);
//            bankNameAction.join();
            return CashFlow.CashFlowBuilder.builder().withKey(cashFlow.getKey()).withCashFlow((int) (cashFlowAmount + 0.5f)).withBankName(bankNameAction.join()).build();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
        return null;
    }

    public static void main(String[] args) {
        LocalTime startTime = LocalTime.now();
        CashFlow cashFlow = CashFlow.CashFlowBuilder.builder().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").build();
        new StaxParser(cashFlow).parse();
        System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }


    public int round(float num) {

        if (num > 0) {
            return (int) (num + 0.5d);
        } else {
            return (int) (num - 0.5d);
        }
    }
}
