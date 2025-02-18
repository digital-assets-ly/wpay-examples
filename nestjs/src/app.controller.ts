import { Body, Controller, Headers, Post, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';
import { WPayPaymentIntent, WPayWebhook } from './types';
import { signPayload } from './utils';

@Controller()
export class AppController {
  constructor(private readonly config: ConfigService) {}

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

    const response = await axios.post(
      'https://api.wpay.ly/api/v1/payment-intents/create-payment-intent',
      paymentIntent,
      {
        headers: {
          Authorization: `Bearer ${apiKey}`,
        },
      },
    );
    const { checkoutUrl, paymentIntentId } = response.data;
    // here you can store the paymentIntentId in your database and show the checkoutUrl to the customer
    return { checkoutUrl, paymentIntentId };
  }
}
