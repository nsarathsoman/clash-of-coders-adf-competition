package hacker;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class StaxParserAmount {

    private final String filePath;
    private final CashFlow cashFlow;
    private float cashFlowAmount = 0f;

    public StaxParserAmount(CashFlow cashFlow) {
        this.cashFlow = cashFlow;
        this.filePath = cashFlow.getDlFileName();
    }

    public void parse() {
        LocalTime startTime = LocalTime.now();
        boolean amount = false;
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileReader(cashFlow.getDlFileName()));
            int event = xmlStreamReader.getEventType();
            while (true) {
                switch (event){
                    case XMLStreamConstants.START_ELEMENT: {
                        if("Amount".equals(xmlStreamReader.getLocalName())){
                            amount = true;
                        }
                        break;
                    }

                    case XMLStreamConstants.CHARACTERS: {
                        if (amount) {
                            cashFlowAmount += Float.valueOf(xmlStreamReader.getText());
                            amount = false;
                        }
                        break;
                    }
                }

                if (!xmlStreamReader.hasNext()) {
                    break;
                }

                event = xmlStreamReader.next();
            }

            cashFlow.setCashFlow((int) (cashFlowAmount + 0.5f));
            cashFlow.setCashFlowAdded(true);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }

    public static void main(String[] args) {
        LocalTime startTime = LocalTime.now();
        CashFlow cashFlow = CashFlow.CashFlowBuilder.builder().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").build();
        new StaxParserAmount(cashFlow).parse();
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
