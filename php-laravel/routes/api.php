<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\WPayController;

Route::post('/wpay/webhook', [WPayController::class, 'webhook']);

Route::post('/payment-intent', [WPayController::class, 'paymentIntent']);