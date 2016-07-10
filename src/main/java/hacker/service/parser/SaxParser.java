package hacker.service.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import hacker.model.CashFlow;
import hacker.util.ValueHolder;


public class SaxParser {

    private static final SaxParser saxParser = new SaxParser();

    private SaxParser() {
    }

    public static SaxParser getInstance() {
        return saxParser;
    }

    public CashFlow parse(final CashFlow cashFlow) {
        final String filePath = cashFlow.getDlFileName();;
        LocalTime startTime = LocalTime.now();
        final ValueHolder<String> routingNumberHolder = new ValueHolder<>();
        final ValueHolder<Float> cashFlowHolder = new ValueHolder<>(0f);
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
                    if (!routingNumberRead && qName.equalsIgnoreCase("RoutingNumberEntered")) {
                        routingNumberEntered = true;
                    }

                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if (amount) {
                        cashFlowHolder.setValue(cashFlowHolder.getValue() + Float.valueOf(new String(ch, start, length)));

                        amount = false;
                    }
                    if (routingNumberEntered) {
                        routingNumberHolder.setValue(new String(ch, start, length));
                        routingNumberEntered = false;
                        routingNumberRead = true;
                    }

                }

            };

            saxParser.parse(filePath, handler);
            CashFlow cashFlowRes = CashFlow.CashFlowBuilder.build().withKey(cashFlow.getKey())
                    .withCashFlow((int)(cashFlowHolder.getValue() + 0.5f))
                    .withBankName(/*BankNameService.getInstance().findBankName(routingNumberHolder.getValue())*/"DUMMY")
                    .done();
            System.out.println("XML Procesing Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
            return cashFlowRes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static void main(String[] args) {
        LocalTime total = LocalTime.now();
        for(int i=0; i<10; i++) {
            LocalTime startTime = LocalTime.now();
            CashFlow cashFlow = CashFlow.CashFlowBuilder.build().withDLFileName("/home/sarath/Downloads/DLxml_3_amount check.xml").done();
            new SaxParser().parse(cashFlow);
            System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
        }
        System.out.println("Total Time Taken : " + ChronoUnit.MILLIS.between(total, LocalTime.now()));
    }

}
