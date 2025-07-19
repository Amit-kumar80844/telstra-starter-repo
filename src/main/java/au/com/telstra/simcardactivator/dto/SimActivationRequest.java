package au.com.telstra.simcardactivator.dto;

public class SimActivationRequest {
    private String icCid;
    private String customerEmail;

    public String getIcCid() { return icCid; }

    public void setIcCid(String icCid) { this.icCid = icCid; }

    public String getCustomerEmail() { return customerEmail; }

    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}

