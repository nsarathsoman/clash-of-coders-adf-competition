package hacker;

/**
 * Created by sarath on 7/6/16.
 */
public class BankAPIResponse {

//    {
//
//        "routingNumber": "061000052",
//
//            "bankName": "Mariamman Indian Bank"
//
//    }

    private String routingNumber;
    private String bankName;

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public String toString() {
        return "BankAPIResponse{" +
                "routingNumber='" + routingNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                '}';
    }
}
