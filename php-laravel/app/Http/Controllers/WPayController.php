<?php

namespace App\Http\Controllers;

use App\Http\Requests\WPayWebhookRequest;
use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Http;

class WPayController extends Controller
{
    public function webhook(Request $request): Response
    {
        $xWebhookSignature = $request->header('x-webhook-signature');
        if (!$xWebhookSignature) {
            return response()->json(['error' => 'Missing signature header'], 400);
        }

        $webhookSecretKey = config('services.wpay.webhook_secret_key');
        $payload = $request->getContent();
        $expectedSignature = hash_hmac('sha256', $payload, $webhookSecretKey);

        if ($expectedSignature !== $xWebhookSignature) {
            return response()->json(['error' => 'Invalid signature'], 401);
        }

        $paymentIntentId = $request->paymentIntentId;
        $status = $request->status;
        $transactionId = $request->metadata['transactionId'];
        $orderId = $request->metadata['orderId'];
        $customerId = $request->metadata['customerId'];
        $customerEmail = $request->metadata['customerEmail'];

        //  Process the webhook data here

        return response('', 200);
    }
  
    public function paymentIntent(Request $request): JsonResponse
    {
        $apiKey = config('services.wpay.api_key');
        $response = Http::withHeaders([
            'Authorization' => 'Bearer '. $apiKey,
        ])->post('https://api.wpay.ly/api/v1/payment-intents/create-payment-intent', [
            'amount' => 350, // this is equal to $3.50 as the amount is in cents
            'metadata' => [
                'transactionId' => 'tx_123',
                'orderId' => 'order_123',
                'customerId' => 'customer_123',
                'customerEmail' => 'example@example.example'
            ]
        ]);
        $checkoutUrl = $response["checkoutUrl"];
        $paymentIntentId = $response["paymentIntentId"];

        // here you can store the paymentIntentId in your database and show the checkoutUrl to the customer

        return response()->json([
                'checkoutUrl' => $checkoutUrl,
                'paymentIntentId' => $paymentIntentId
        ]);
    }
}
