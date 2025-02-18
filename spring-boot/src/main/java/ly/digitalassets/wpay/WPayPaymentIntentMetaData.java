package ly.digitalassets.wpay;

import org.springframework.lang.Nullable;

public class WPayPaymentIntentMetaData {
    @Nullable
    private String transactionId;
    @Nullable
    private String orderId;
    @Nullable
    private String customerId;
    @Nullable
    private String customerEmail;

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    @Override
    public String toString() {
        return "WPayPaymentIntentMetaData [transactionId=" + transactionId + ", orderId=" + orderId + ", customerId="
                + customerId + ", customerEmail=" + customerEmail + "]";
    }

}
