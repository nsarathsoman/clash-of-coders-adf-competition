package hacker;

/**
 * Created by sarath on 7/4/16.
 */
public class CashFlow {

    private String key;
    private String dlFileName;
    private int cashFlow;
    private boolean cashFlowAdded;
    private String bankName;
    private String routingNumber;
    private boolean recievedbankName;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDlFileName() {
        return dlFileName;
    }

    public void setDlFileName(String dlFileName) {
        this.dlFileName = dlFileName;
    }

    public int getCashFlow() {
        return cashFlow;
    }

    public void setCashFlow(int cashFlow) {
        this.cashFlow = cashFlow;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public boolean isRecievedbankName() {
        return recievedbankName;
    }

    public void setRecievedbankName(boolean recievedbankName) {
        this.recievedbankName = recievedbankName;
    }

    public boolean isCashFlowAdded() {
        return cashFlowAdded;
    }

    public void setCashFlowAdded(boolean cashFlowAdded) {
        this.cashFlowAdded = cashFlowAdded;
    }

    @Override
    public String toString() {
        return "{" +
                "\"key\":\"" + key + "\"" +
                ", \"cashFlow\":\"" + cashFlow + "\"" +
                ", \"bankName\":\"" + bankName + "\"" +
                "}";
    }

    public static final class CashFlowBuilder {
        private final CashFlow cashFlow;

        public static CashFlowBuilder builder() {
            return new CashFlowBuilder(new CashFlow());
        }

        private CashFlowBuilder(CashFlow cashFlow) {
            this.cashFlow = cashFlow;
        }

        public CashFlowBuilder withKey(String key) {
            cashFlow.setKey(key);
            return this;
        }

        public CashFlowBuilder withDLFileName(String dlFileName) {
            cashFlow.setDlFileName(dlFileName);
            return this;
        }

        public CashFlowBuilder withCashFlow(int cashFlo) {
            cashFlow.setCashFlow(cashFlo);
            return this;
        }

        public CashFlowBuilder withBankName(String bankName) {
            cashFlow.setBankName(bankName);
            return this;
        }

        public  CashFlow build(){
            return cashFlow;
        }

        public CashFlowBuilder withRoutingNumber(String routingNumber) {
            cashFlow.setRoutingNumber(routingNumber);
            return this;
        }
    }
}
