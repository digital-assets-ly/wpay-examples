import { createHmac } from 'node:crypto';

export function signPayload(payload: any, secret: string): string {
  const payloadString = JSON.stringify(payload);
  return createHmac('sha256', secret).update(payloadString).digest('hex');
}
