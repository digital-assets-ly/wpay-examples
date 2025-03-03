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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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

		WPayPaymentIntent wPayPaymentIntent = new WPayPaymentIntent(350) // the amount in cents, e.g. 350 for $3.50
				.withCustomerEmail("example@example.example")
				.withOrderId("order_123")
				.withTransactionId("tx_123")
				.withCustomerId("customer_123");

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(wPayApiKey);

		// Sandbox mode for testing
		// When true, requests go to WPay's sandbox environment
		// Perfect for testing without real transactions
		// Set to false or remove for production
		headers.set("sandbox", "true");

		HttpEntity<WPayPaymentIntent> requestEntity = new HttpEntity<>(wPayPaymentIntent, headers);

		ResponseEntity<WPayPaymentIntentResponse> response;
		try {
			response = restTemplate.exchange(
					"https://api.wpay.ly/api/v1/payment-intents/create-payment-intent",
					HttpMethod.POST, requestEntity,
					WPayPaymentIntentResponse.class);
			// Store the paymentIntentId in your database for transaction tracking
			// Redirect the customer to the checkoutUrl to complete their payment
			return ResponseEntity.ok(response.getBody());
		} catch (HttpServerErrorException e) {
			// Log the error with details for troubleshooting
			System.err.println("WPay API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
				// HTTP 503: Service Unavailable
				// The WPay service is temporarily unavailable or undergoing maintenance
				// Action: Implement retry mechanism with exponential backoff for transient
				// errors
				// User message: "Our payment processor is temporarily unavailable. Please try
				// again in a few minutes."
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				// HTTP 401: Unauthorized
				// Authentication failed - likely due to invalid or expired API key
				// Action: Verify API key configuration and validity in the WPay dashboard
				// Log this as a critical error requiring immediate attention
				// User message: "We're experiencing issues with our payment system. Our team
				// has been notified."
			} else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
				// HTTP 500: Internal Server Error
				// WPay is experiencing internal issues
				// Action: Monitor WPay status page and consider implementing a fallback payment
				// method
				// User message: "We're experiencing temporary issues processing payments.
				// Please try again later."
			}
		} catch (HttpClientErrorException e) {
			System.err.println(
					"Client error when calling WPay API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
				// HTTP 429: Too Many Requests
				// Rate limit exceeded - your application is making too many requests
				// Action: Implement rate limiting in your application to stay within WPay's
				// limits
				// Consider increasing your API tier if this happens frequently
				// User message: "Our payment system is currently busy. Please try again in a
				// moment."
			} else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				// HTTP 400: Bad Request
				// Request contains invalid parameters or is malformed
				// Action: Check the request payload against WPay's API documentation
				// User message: "There was an issue with your payment information. Please
				// verify and try again."
			}
		} catch (Exception e) {
			// Catch any other unexpected exceptions
			System.err.println("Unexpected error in payment processing: " + e.getMessage());
			// Action: Set up monitoring to detect and alert on unexpected exceptions
			// User message: "An unexpected error occurred. Please try again or contact
			// support."
		}

		return ResponseEntity.internalServerError().build();
	}

	/**
	 * Creates a wallet payment intent for WPay integration.
	 * 
	 * Unlike the standard {@link #createPaymentIntent()} method, this endpoint is
	 * specifically
	 * designed for wallet integrations where the amount is determined dynamically
	 * by the user
	 * at the time of payment. There is no need to send a predefined amount as part
	 * of the request,
	 * allowing users to pay as much as they need from their wallet.
	 * 
	 * @return ResponseEntity containing the WPay payment intent response with
	 *         checkout URL
	 */
	@PostMapping(value = "/api/wallet-payment-intent")
	public ResponseEntity<WPayPaymentIntentResponse> createWalletPaymentIntent() {

		WPayPaymentIntentMetaData wPayPaymentIntentMetaData = new WPayPaymentIntentMetaData();
		wPayPaymentIntentMetaData.setCustomerEmail("example@example.example");
		wPayPaymentIntentMetaData.setOrderId("order_123");
		wPayPaymentIntentMetaData.setTransactionId("tx_123");
		wPayPaymentIntentMetaData.setCustomerId("customer_123");

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(wPayApiKey);

		// Sandbox mode for testing
		// When true, requests go to WPay's sandbox environment
		// Perfect for testing without real transactions
		// Set to false or remove for production
		headers.set("sandbox", "true");

		HttpEntity<WPayPaymentIntentMetaData> requestEntity = new HttpEntity<>(wPayPaymentIntentMetaData, headers);

		ResponseEntity<WPayPaymentIntentResponse> response;
		try {
			response = restTemplate.exchange(
					"https://api.wpay.ly/api/v1/payment-intents/create-wallet-payment-intent",
					HttpMethod.POST, requestEntity,
					WPayPaymentIntentResponse.class);
			// Store the paymentIntentId in your database for transaction tracking
			// Redirect the customer to the checkoutUrl to complete their payment
			return ResponseEntity.ok(response.getBody());
		} catch (HttpServerErrorException e) {
			// Log the error with details for troubleshooting
			System.err.println("WPay API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
				// HTTP 503: Service Unavailable
				// The WPay service is temporarily unavailable or undergoing maintenance
				// Action: Implement retry mechanism with exponential backoff for transient
				// errors
				// User message: "Our payment processor is temporarily unavailable. Please try
				// again in a few minutes."
			} else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				// HTTP 401: Unauthorized
				// Authentication failed - likely due to invalid or expired API key
				// Action: Verify API key configuration and validity in the WPay dashboard
				// Log this as a critical error requiring immediate attention
				// User message: "We're experiencing issues with our payment system. Our team
				// has been notified."
			} else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
				// HTTP 500: Internal Server Error
				// WPay is experiencing internal issues
				// Action: Monitor WPay status page and consider implementing a fallback payment
				// method
				// User message: "We're experiencing temporary issues processing payments.
				// Please try again later."
			}
		} catch (HttpClientErrorException e) {
			System.err.println(
					"Client error when calling WPay API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

			if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
				// HTTP 429: Too Many Requests
				// Rate limit exceeded - your application is making too many requests
				// Action: Implement rate limiting in your application to stay within WPay's
				// limits
				// Consider increasing your API tier if this happens frequently
				// User message: "Our payment system is currently busy. Please try again in a
				// moment."
			} else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
				// HTTP 400: Bad Request
				// Request contains invalid parameters or is malformed
				// Action: Check the request payload against WPay's API documentation
				// User message: "There was an issue with your payment information. Please
				// verify and try again."
			}
		} catch (Exception e) {
			// Catch any other unexpected exceptions
			System.err.println("Unexpected error in payment processing: " + e.getMessage());
			// Action: Set up monitoring to detect and alert on unexpected exceptions
			// User message: "An unexpected error occurred. Please try again or contact
			// support."
		}

		return ResponseEntity.internalServerError().build();
	}

}
