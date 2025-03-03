import { Body, Controller, Headers, Post, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios, { isAxiosError } from 'axios';
import { WPayPaymentIntent, WPayWebhook } from './types';
import { signPayload } from './utils';

@Controller()
export class AppController {
  constructor(private readonly config: ConfigService) { }

  @Post('/api/wpay/webhook')
  webhook(
    @Body() wPayWebhook: WPayWebhook,
    @Headers('x-webhook-signature') xWebhookSignature: string,
  ) {
    const webhookSecretKey = this.config.getOrThrow('WPAY_WEBHOOK_SECRET_KEY');
    const expectedSignature = signPayload(wPayWebhook, webhookSecretKey);

    if (xWebhookSignature !== expectedSignature) {
      throw new UnauthorizedException('Invalid signature');
    }
    // from here you can handle the webhook data
    console.log(wPayWebhook);
  }

  @Post('/api/payment-intent')
  async createPaymentIntent() {
    const apiKey = this.config.getOrThrow('WPAY_API_KEY');

    const paymentIntent: WPayPaymentIntent = {
      amount: 350, // this is equal to $3.50 as the amount is in cents
      metadata: {
        transactionId: 'tx_123',
        orderId: 'order_123',
        customerId: 'customer_123',
        customerEmail: 'example@example.com',
      },
    };

    try {
      const response = await axios.post(
        'https://api.wpay.ly/api/v1/payment-intents/create-payment-intent',
        paymentIntent,
        {
          headers: {
            Authorization: `Bearer ${apiKey}`,

            // Sandbox mode for testing
            // When true, requests go to WPay's sandbox environment
            // Perfect for testing without real transactions
            // Set to false or remove for production
            sandbox: 'true',
          },
        },
      );
      const { checkoutUrl, paymentIntentId } = response.data;
      // Store the paymentIntentId in your database for transaction tracking
      // Redirect the customer to the checkoutUrl to complete their payment
      return { checkoutUrl, paymentIntentId };
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
    }
  }
}
