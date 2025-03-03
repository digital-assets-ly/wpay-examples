export enum PaymentIntentStatus {
  new = 'new',
  detected = 'detected',
  confirmed = 'confirmed',
  failed = 'failed',
  expired = 'expired',
  void = 'void',
  refunded = 'refunded',
}

export type PaymentIntentMetaDataModel = {
  transactionId?: string;
  orderId?: string;
  customerId?: string;
  customerEmail?: string;
};

export type WPayWebhook = {
  paymentIntentId: string;
  status: PaymentIntentStatus;
  metadata: PaymentIntentMetaDataModel;
};

export class WPayPaymentIntentMetaData {
  transactionId?: string;
  orderId?: string;
  customerId?: string;
  customerEmail?: string;
}

export class WPayPaymentIntent {
  amount: number;
  metadata: WPayPaymentIntentMetaData;
}
