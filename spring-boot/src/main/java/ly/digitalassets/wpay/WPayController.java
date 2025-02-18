package ly.digitalassets.wpay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class WPayController {

	final RestTemplate restTemplate;

	@Value("${wpay.api.key}")
	private String wPayApiKey;

	@Value("${wpay.webhook.secret.key}")
	private String webhookSecretKey;

	public WPayController() {
		this.restTemplate = new RestTemplate();

	}

	// should return 200 OK on success
	@PostMapping("/api/wpay/webhook")
	public ResponseEntity<Void> webhook(@RequestBody WPayWebhook wPayWebhook,
			@RequestHeader("x-webhook-signature") String xWebhookSignature) {
		try {
			String expectedSignature = Utils.hmacSignPayload(wPayWebhook, webhookSecretKey);
			if (!expectedSignature.equals(xWebhookSignature)) {
				System.err.println("Signature does not match");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			// from here you can handle the webhook data
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/api/payment-intent")
	public ResponseEntity<WPayPaymentIntentResponse> createPaymentIntent() {

		WPayPaymentIntent wPayPaymentIntent = new WPayPaymentIntent(350) // this is equal to $3.50 as the amount is in cents
				.withCustomerEmail("example@example.example")
				.withOrderId("order_123")
				.withTransactionId("tx_123").withCustomerId("customer_123");

		// Initialize HTTP headers for WPay API request
		HttpHeaders headers = new HttpHeaders();

		// Set Bearer Authentication token using the WPay API key
		// This is required for all API requests to authenticate with the WPay server
		// The API key should be kept secure and not exposed in the codebase
		headers.setBearerAuth(wPayApiKey);

		// Enable sandbox mode for testing
		// When true, requests are routed to WPay's sandbox environment
		// This allows for testing integration without processing real transactions
		// Should be set to false in production environment
		headers.set("sandbox", "true");

		HttpEntity<WPayPaymentIntent> requestEntity = new HttpEntity<>(wPayPaymentIntent, headers);

		ResponseEntity<WPayPaymentIntentResponse> response = restTemplate.exchange(
				"https://api.wpay.ly/api/v1/payment-intents/create-payment-intent",
				HttpMethod.POST, requestEntity,
				WPayPaymentIntentResponse.class);

		// here you can store the response body paymentIntentId in your database and show the response body checkoutUrl to the customer

		return ResponseEntity.ok(response.getBody());
	}

}
