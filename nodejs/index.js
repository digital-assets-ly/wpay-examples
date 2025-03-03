require("dotenv").config();
const express = require("express");
const { createHmac } = require("crypto");
const { default: axios, isAxiosError } = require("axios");
const app = express();
app.use(express.json());

// should return 200 OK on success
app.post("/api/wpay/webhook", (req, res) => {
  const webhookSecretKey = process.env.WPAY_WEBHOOK_SECRET_KEY;
  const xWebhookSignature = req.header("x-webhook-signature");
  const expectedSignature = signPayload(req.body, webhookSecretKey);

  if (xWebhookSignature !== expectedSignature) {
    return res.sendStatus(401);
  }
  const { paymentIntentId, status, amountInCents, metadata } = req.body;
  const { transactionId, orderId, customerId, customerEmail } = metadata;
  // from here you can handle the webhook data
  console.log({
    paymentIntentId,
    status,
    amountInCents,
    metadata: { transactionId, orderId, customerId, customerEmail },
  });

  res.sendStatus(200);
});

app.post("/api/wallet-payment-intent", async (req, res) => {
  const apiKey = process.env.WPAY_API_KEY;

  const paymentIntentMetadata = {
    transactionId: "tx_123", // optional
    orderId: "order_123", // optional
    customerId: "customer_123", // optional
    customerEmail: "example@example.com", // optional
  };
  try {
    const response = await axios.post(
      "https://api.wpay.ly/api/v1/payment-intents/create-wallet-payment-intent",
      paymentIntentMetadata,
      {
        headers: {
          Authorization: `Bearer ${apiKey}`,

          // Sandbox mode for testing
          // When true, requests go to WPay's sandbox environment
          // Perfect for testing without real transactions
          // Set to false or remove for production
          sandbox: "true",
        },
      }
    );
    const { checkoutUrl, paymentIntentId } = response.data;
    // Store the paymentIntentId in your database for transaction tracking
    // Redirect the customer to the checkoutUrl to complete their payment
    res.send({ checkoutUrl, paymentIntentId });
  } catch (error) {
    if (isAxiosError(error)) {
      if (error.response) {
        if (error.response.status === 503) {
          // HTTP 503: Service Unavailable
          // The WPay service is temporarily unavailable or undergoing maintenance
          // Action: Implement retry mechanism with exponential backoff for transient
          // errors
          // User message: "Our payment processor is temporarily unavailable. Please try
          // again in a few minutes."
        } else if (error.response.status === 401) {
          // HTTP 401: Unauthorized
          // Authentication failed - likely due to invalid or expired API key
          // Action: Verify API key configuration and validity in the WPay dashboard
          // Log this as a critical error requiring immediate attention
          // User message: "We're experiencing issues with our payment system. Our team
          // has been notified."
        } else if (error.response.status === 500) {
          // HTTP 500: Internal Server Error
          // WPay is experiencing internal issues
          // Action: Monitor WPay status page and consider implementing a fallback payment
          // method
          // User message: "We're experiencing temporary issues processing payments.
          // Please try again later."
        } else if (error.response.status === 429) {
          // HTTP 429: Too Many Requests
          // Rate limit exceeded - your application is making too many requests
          // Action: Implement rate limiting in your application to stay within WPay's
          // limits
          // Consider increasing your API tier if this happens frequently
          // User message: "Our payment system is currently busy. Please try again in a
          // moment."
        } else if (error.response.status === 400) {
          // HTTP 400: Bad Request
          // Request contains invalid parameters or is malformed
          // Action: Check the request payload against WPay's API documentation
          // User message: "There was an issue with your payment information. Please
          // verify and try again."
        } else {
          // Catch any other unexpected exceptions
          // Action: Set up monitoring to detect and alert on unexpected exceptions
          // User message: "An unexpected error occurred. Please try again or contact
          // support."
        }
      }
    }
    res.status(500).send();
  }
});

app.post("/api/payment-intent", async (req, res) => {
  const apiKey = process.env.WPAY_API_KEY;

  const paymentIntent = {
    amount: 350, // this is equal to $3.50 as the amount is in cents
    metadata: {
      transactionId: "tx_123",
      orderId: "order_123",
      customerId: "customer_123",
      customerEmail: "example@example.com",
    },
  };
  try {
    const response = await axios.post(
      "https://api.wpay.ly/api/v1/payment-intents/create-payment-intent",
      paymentIntent,
      {
        headers: {
          Authorization: `Bearer ${apiKey}`,

          // Sandbox mode for testing
          // When true, requests go to WPay's sandbox environment
          // Perfect for testing without real transactions
          // Set to false or remove for production
          sandbox: "true",
        },
      }
    );
    const { checkoutUrl, paymentIntentId } = response.data;
    // Store the paymentIntentId in your database for transaction tracking
    // Redirect the customer to the checkoutUrl to complete their payment
    res.send({ checkoutUrl, paymentIntentId });
  } catch (error) {
    if (isAxiosError(error)) {
      if (error.response) {
        if (error.response.status === 503) {
          // HTTP 503: Service Unavailable
          // The WPay service is temporarily unavailable or undergoing maintenance
          // Action: Implement retry mechanism with exponential backoff for transient
          // errors
          // User message: "Our payment processor is temporarily unavailable. Please try
          // again in a few minutes."
        } else if (error.response.status === 401) {
          // HTTP 401: Unauthorized
          // Authentication failed - likely due to invalid or expired API key
          // Action: Verify API key configuration and validity in the WPay dashboard
          // Log this as a critical error requiring immediate attention
          // User message: "We're experiencing issues with our payment system. Our team
          // has been notified."
        } else if (error.response.status === 500) {
          // HTTP 500: Internal Server Error
          // WPay is experiencing internal issues
          // Action: Monitor WPay status page and consider implementing a fallback payment
          // method
          // User message: "We're experiencing temporary issues processing payments.
          // Please try again later."
        } else if (error.response.status === 429) {
          // HTTP 429: Too Many Requests
          // Rate limit exceeded - your application is making too many requests
          // Action: Implement rate limiting in your application to stay within WPay's
          // limits
          // Consider increasing your API tier if this happens frequently
          // User message: "Our payment system is currently busy. Please try again in a
          // moment."
        } else if (error.response.status === 400) {
          // HTTP 400: Bad Request
          // Request contains invalid parameters or is malformed
          // Action: Check the request payload against WPay's API documentation
          // User message: "There was an issue with your payment information. Please
          // verify and try again."
        } else {
          // Catch any other unexpected exceptions
          // Action: Set up monitoring to detect and alert on unexpected exceptions
          // User message: "An unexpected error occurred. Please try again or contact
          // support."
        }
      }
    }
    res.status(500).send();
  }
});

app.listen(8080, () => {
  console.log(`Example app listening on port http://localhost:8080/api`);
});

function signPayload(payload, secret) {
  const payloadString = JSON.stringify(payload);
  return createHmac("sha256", secret).update(payloadString).digest("hex");
}
