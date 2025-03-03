package ly.digitalassets.wpay;

public class WPayPaymentIntentResponse {
    private String checkoutUrl;
    private String paymentIntentId;

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

}
