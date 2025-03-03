package ly.digitalassets.wpay;

public class WPayPaymentIntent {

    /**
     * The amount of the payment in cents. The currency is determined by
     * the account settings in the WPay Dashboard. For example, a value of
     * 350 represents $3.50.
     */
    final Integer amount;

    final WPayPaymentIntentMetaData metadata;

    /**
     * Constructs a new WPayPaymentIntent with the given amount in cents.
     *
     * @param amount the amount of the payment in cents, e.g. 350 for $3.50
     */
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
