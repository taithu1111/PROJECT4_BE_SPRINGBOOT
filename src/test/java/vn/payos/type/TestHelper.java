package vn.payos.type;

import java.util.List;

public class TestHelper {
    public static CheckoutResponseData createCheckoutResponseData(
            String bin, String accountNumber, String accountName, Integer amount,
            String description, Long orderCode, String currency, String paymentLinkId,
            String status, String checkoutUrl, String qrCode) {
        return new CheckoutResponseData(bin, accountNumber, accountName, amount, description, orderCode, currency,
                paymentLinkId, status, checkoutUrl, qrCode);
    }

    public static PaymentLinkData createPaymentLinkData(
            String id, Long orderCode, Integer amount, Integer amountPaid,
            Integer amountRemaining, String status, String createdAt,
            List<Transaction> transactions, String cancellationReason, String cancelledAt) {
        return new PaymentLinkData(id, orderCode, amount, amountPaid, amountRemaining, status, createdAt, transactions,
                cancellationReason, cancelledAt);
    }
}
