package ly.digitalassets.wpay;

public class WPayPaymentIntent {
    final Integer amount;
    final WPayPaymentIntentMetaData metadata;

    public WPayPaymentIntent(Integer amount) {
        this.amount = amount;
        this.metadata = new WPayPaymentIntentMetaData();
    }

    public WPayPaymentIntent withOrderId(String orderId) {
        this.metadata.setOrderId(orderId);
        return this;
    }

    public WPayPaymentIntent withCustomerId(String customerId) {
        this.metadata.setCustomerId(customerId);
        return this;
    }

    public WPayPaymentIntent withCustomerEmail(String customerEmail) {
        this.metadata.setCustomerEmail(customerEmail);
        return this;
    }

    public WPayPaymentIntent withTransactionId(String transactionId) {
        this.metadata.setTransactionId(transactionId);
        return this;
    }

    public Integer getAmount() {
        return amount;
    }

    public WPayPaymentIntentMetaData getMetadata() {
        return metadata;
    }

}
