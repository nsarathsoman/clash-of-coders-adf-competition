package hacker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class SaxParser {

    private final String filePath;
    private final CashFlow cashFlow;
    private float cashFlowAmount = 0f;

    public SaxParser(CashFlow cashFlow) {
        this.cashFlow = cashFlow;
        this.filePath = cashFlow.getDlFileName();
    }

    public void parse() {
        LocalTime startTime = LocalTime.now();
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                boolean amount = false;
                boolean routingNumberEntered = false;
                boolean routingNumberRead = false;

                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {

                    if (qName.equalsIgnoreCase("Amount")) {
                        amount = true;
                    }
                    if (qName.equalsIgnoreCase("RoutingNumberEntered")) {
                        routingNumberEntered = true;
                    }

                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if (amount) {
                        cashFlowAmount += Float.valueOf(new String(ch, start, length));
                        amount = false;
                    }
                    if (routingNumberEntered) {
                        CashFlowService.getInstance().findBankName(new String(ch, start, length), cashFlow);
                        routingNumberEntered = false;
                    }

                }

            };

            saxParser.parse(cashFlow.getDlFileName(), handler);
            cashFlow.setCashFlow(cashFlowAmount);
            cashFlow.setCashFlowAdded(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }

    public static void main(String[] args) {
        LocalTime startTime = LocalTime.now();
        CashFlow cashFlow = CashFlow.CashFlowBuilder.builder().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").build();
        new SaxParser(cashFlow).parse();
        System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }

}
