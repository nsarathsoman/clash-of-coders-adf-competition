package hacker.service.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import hacker.model.CashFlow;
import hacker.service.BankNameService;


public class SaxParser implements Parser{

    private static final SaxParser saxParser = new SaxParser();

    private SaxParser() {
    }

    public static SaxParser getInstance() {
        return saxParser;
    }

    public CashFlow parse(final CashFlow cashFlow) {
        final String filePath = cashFlow.getDlFileName();;
        LocalTime startTime = LocalTime.now();
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            final CashFlow cashFlowRes = new CashFlow();
            DefaultHandler handler = new DefaultHandler() {
                public String routingNumber;
                float cashFlowAmount = 0f;
                boolean amount = false;
                boolean routingNumberEntered = false;
                boolean routingNumberRead = false;

                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {

                    if (qName.equalsIgnoreCase("Amount")) {
                        amount = true;
                    }
                    if (!routingNumberRead && qName.equalsIgnoreCase("RoutingNumberEntered")) {
                        routingNumberEntered = true;
                    }

                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if (amount) {
                        cashFlowAmount += Float.valueOf(new String(ch, start, length));
                        cashFlowRes.setCashFlow((int) (cashFlowAmount + 0.5f));
                        amount = false;
                    }
                    if (routingNumberEntered) {
                        routingNumber = new String(ch, start, length);
                        cashFlowRes.setRoutingNumber(routingNumber);
                        routingNumberEntered = false;
                        routingNumberRead = true;
                    }

                }

            };

            saxParser.parse(filePath, handler);
            cashFlowRes.setKey(cashFlow.getKey());
            cashFlowRes.setBankName(BankNameService.getInstance().findBankName(cashFlowRes.getRoutingNumber()));
            System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
            return cashFlowRes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

}
