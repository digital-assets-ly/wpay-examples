require("dotenv").config();
const express = require("express");
const { createHmac } = require("crypto");
const { default: axios } = require("axios");
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
  const { paymentIntentId, status, metadata } = req.body;
  const { transactionId, orderId, customerId, customerEmail } = metadata;
  // from here you can handle the webhook data
  console.log({
    paymentIntentId,
    status,
    metadata: { transactionId, orderId, customerId, customerEmail },
  });

  res.sendStatus(200);
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

  const response = await axios.post(
    "https://api.wpay.ly/api/v1/payment-intents/create-payment-intent",
    paymentIntent,
    {
      headers: {
        Authorization: `Bearer ${apiKey}`,
      },
    }
  );
  const { checkoutUrl, paymentIntentId } = response.data;
  // here you can store the paymentIntentId in your database and show the checkoutUrl to the customer
  res.send({ checkoutUrl, paymentIntentId });
});

app.listen(8080, () => {
  console.log(`Example app listening on port http://localhost:8080/api`);
});

function signPayload(payload, secret) {
  const payloadString = JSON.stringify(payload);
  return createHmac("sha256", secret).update(payloadString).digest("hex");
}
