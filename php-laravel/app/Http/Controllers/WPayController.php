<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Illuminate\Http\JsonResponse;
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
        $amountInCents = $request->amountInCents;
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
            'Authorization' => 'Bearer ' . $apiKey,
            // Sandbox mode for testing
            // When true, requests go to WPay's sandbox environment
            // Perfect for testing without real transactions
            // Set to false or remove for production
            'sandbox' => 'true',
        ])->post('https://api.wpay.ly/api/v1/payment-intents/create-payment-intent', [
            'amount' => 350, // this is equal to $3.50 as the amount is in cents
            'metadata' => [
                'transactionId' => 'tx_123',
                'orderId' => 'order_123',
                'customerId' => 'customer_123',
                'customerEmail' => 'example@example.example'
            ]
        ]);
        if ($response->successful()) {
            $paymentIntentId = $response["paymentIntentId"];
            $checkoutUrl = $response["checkoutUrl"];

            // Store the paymentIntentId in your database for transaction tracking
            // Redirect the customer to the checkoutUrl to complete their payment

            return response()->json([
                'checkoutUrl' => $checkoutUrl,
                'paymentIntentId' => $paymentIntentId
            ]);
        } else if ($response->status() === 429) {
            // HTTP 429: Too Many Requests
            // Rate limit exceeded - your application is making too many requests
            // Action: Implement rate limiting in your application to stay within WPay's
            // limits
            // Consider increasing your API tier if this happens frequently
            // User message: "Our payment system is currently busy. Please try again in a
            // moment."
        } else if ($response->status() === 503) {
            // HTTP 503: Service Unavailable
            // The WPay service is temporarily unavailable or undergoing maintenance
            // Action: Implement retry mechanism with exponential backoff for transient
            // errors
            // User message: "Our payment processor is temporarily unavailable. Please try
            // again in a few minutes."
        } else if ($response->status() === 400) {
            // HTTP 400: Bad Request
            // Request contains invalid parameters or is malformed
            // Action: Check the request payload against WPay's API documentation
            // User message: "There was an issue with your payment information. Please
            // verify and try again."
        } else if ($response->status() === 401) {
            // HTTP 401: Unauthorized
            // Authentication failed - likely due to invalid or expired API key
            // Action: Verify API key configuration and validity in the WPay dashboard
            // Log this as a critical error requiring immediate attention
            // User message: "We're experiencing issues with our payment system. Our team
            // has been notified."
        } else if ($response->status() === 500) {
            // HTTP 500: Internal Server Error
            // WPay is experiencing internal issues
            // Action: Monitor WPay status page and consider implementing a fallback payment
            // method
            // User message: "We're experiencing temporary issues processing payments.
            // Please try again later."
        } else {
            // Catch any other unexpected exceptions
            // Action: Set up monitoring to detect and alert on unexpected exceptions
            // User message: "An unexpected error occurred. Please try again or contact
            // support."
        }
        return response()->json(['error' => 'An unexpected error occurred'], 500);
    }
    
    public function walletPaymentIntent(Request $request): JsonResponse
    {
        $apiKey = config('services.wpay.api_key');
        $response = Http::withHeaders([
            'Authorization' => 'Bearer ' . $apiKey,
            // Sandbox mode for testing
            // When true, requests go to WPay's sandbox environment
            // Perfect for testing without real transactions
            // Set to false or remove for production
            'sandbox' => 'true',
        ])->post('https://api.wpay.ly/api/v1/payment-intents/create-wallet-payment-intent', [
            'transactionId' => 'tx_123', //optional
            'orderId' => 'order_123', //optional
            'customerId' => 'customer_123', //optional
            'customerEmail' => 'example@example.example' //optional
        ]);
        if ($response->successful()) {
            $paymentIntentId = $response["paymentIntentId"];
            $checkoutUrl = $response["checkoutUrl"];

            // Store the paymentIntentId in your database for transaction tracking
            // Redirect the customer to the checkoutUrl to complete their payment

            return response()->json([
                'checkoutUrl' => $checkoutUrl,
                'paymentIntentId' => $paymentIntentId
            ]);
        } else if ($response->status() === 429) {
            // HTTP 429: Too Many Requests
            // Rate limit exceeded - your application is making too many requests
            // Action: Implement rate limiting in your application to stay within WPay's
            // limits
            // Consider increasing your API tier if this happens frequently
            // User message: "Our payment system is currently busy. Please try again in a
            // moment."
        } else if ($response->status() === 503) {
            // HTTP 503: Service Unavailable
            // The WPay service is temporarily unavailable or undergoing maintenance
            // Action: Implement retry mechanism with exponential backoff for transient
            // errors
            // User message: "Our payment processor is temporarily unavailable. Please try
            // again in a few minutes."
        } else if ($response->status() === 400) {
            // HTTP 400: Bad Request
            // Request contains invalid parameters or is malformed
            // Action: Check the request payload against WPay's API documentation
            // User message: "There was an issue with your payment information. Please
            // verify and try again."
        } else if ($response->status() === 401) {
            // HTTP 401: Unauthorized
            // Authentication failed - likely due to invalid or expired API key
            // Action: Verify API key configuration and validity in the WPay dashboard
            // Log this as a critical error requiring immediate attention
            // User message: "We're experiencing issues with our payment system. Our team
            // has been notified."
        } else if ($response->status() === 500) {
            // HTTP 500: Internal Server Error
            // WPay is experiencing internal issues
            // Action: Monitor WPay status page and consider implementing a fallback payment
            // method
            // User message: "We're experiencing temporary issues processing payments.
            // Please try again later."
        } else {
            // Catch any other unexpected exceptions
            // Action: Set up monitoring to detect and alert on unexpected exceptions
            // User message: "An unexpected error occurred. Please try again or contact
            // support."
        }
        return response()->json(['error' => 'An unexpected error occurred'], 500);
    }
}
