package hacker;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;


public class VTDDLXMLFileProcessor {

    private final String filePath;
    private final CashFlow cashFlow;

    public VTDDLXMLFileProcessor(CashFlow cashFlow) {
        this.cashFlow = cashFlow;
        this.filePath = cashFlow.getDlFileName();
    }

    public CashFlow parse() {
        try {
//            VTDNav vtdNav = new VTDNav();
            VTDGen vg = new VTDGen();
            if(vg.parseFile(filePath, false)) {
                VTDNav vn = vg.getNav();
                AutoPilot ap = new AutoPilot(vn);
                ap.selectXPath("RoutingNumberEntered");
//                ap.selectElement("");
                System.out.println(" Text ==> "+vn.toNormalizedString(vn.getText()));
                ap.selectElement("Amount");

                while(ap.iterate()){
                    System.out.println(" Text ==> "+vn.toNormalizedString(vn.getText()));
                }
            }
        } catch (Exception e) {
            System.out.println("exception occurred ==>" + e);
        }
        return null;
    }

    public static void main(String[] args) {
        LocalTime startTime = LocalTime.now();
        CashFlow cashFlow = CashFlow.CashFlowBuilder.builder().withDLFileName("/home/sarath/Downloads/dlxmlCashFlowMonthlyOne-VE(1).xml").build();
        new VTDDLXMLFileProcessor(cashFlow).parse();
        System.out.println("Time Taken : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
    }

}
