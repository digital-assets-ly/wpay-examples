<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class WPayWebhookRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, \Illuminate\Contracts\Validation\ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'paymentIntentId' => ['required', 'string'],
            'status' => ['required', 'string'],
            'metadata.transactionId' => ['nullable', 'string'],
            'metadata.orderId' => ['nullable', 'string'],
            'metadata.customerId' => ['nullable', 'string'],
            'metadata.customerEmail' => ['nullable', 'string'],
        ];
    }
}
