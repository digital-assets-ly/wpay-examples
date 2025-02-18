package ly.digitalassets.wpay;

import org.springframework.lang.Nullable;

public class WPayWebhook {
    private String paymentIntentId;
    private String status;
    @Nullable
    private WPayPaymentIntentMetaData metadata;

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public WPayPaymentIntentMetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(WPayPaymentIntentMetaData metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "WPayWebhook [paymentIntentId=" + paymentIntentId + ", status=" + status + ", metadata=" + metadata
                + "]";
    }

}
