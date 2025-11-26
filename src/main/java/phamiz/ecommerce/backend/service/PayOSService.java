package phamiz.ecommerce.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.model.OrderItem;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayOSService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private PayOS payOS;

    public PayOS getPayOS() {
        if (payOS == null) {
            payOS = new PayOS(clientId, apiKey, checksumKey);
        }
        return payOS;
    }

    public CheckoutResponseData createPaymentLink(Order order) throws Exception {
        PayOS payOS = getPayOS();

        List<ItemData> items = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            ItemData item = ItemData.builder()
                    .name(orderItem.getProduct().getProduct_name())
                    .quantity(orderItem.getQuantity())
                    .price((int) orderItem.getPrice())
                    .build();
            items.add(item);
        }

        String description = "Thanh toan don hang " + order.getId();
        // PayOS requires description to be less than 25 characters
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // Ensure orderCode is a valid long. Using current timestamp + order id to
        // ensure uniqueness and long type
        // Note: PayOS orderCode must be a number.
        // We can use the order ID directly if it's unique enough, but PayOS might have
        // restrictions.
        // Let's use System.currentTimeMillis() for now to be safe, or just order ID if
        // we can ensure it's not reused.
        // Better to use order ID but we need to handle if it's too large or small.
        // Let's try using order ID directly.
        long orderCode = order.getId();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount((int) order.getTotalPrice())
                .description(description)
                .returnUrl("http://localhost:3000/payment/success") // Frontend URL
                .cancelUrl("http://localhost:3000/payment/cancel") // Frontend URL
                .items(items)
                .build();

        return payOS.createPaymentLink(paymentData);
    }

    public PaymentLinkData getPaymentLinkInformation(long orderId) throws Exception {
        PayOS payOS = getPayOS();
        return payOS.getPaymentLinkInformation(orderId);
    }

    public String confirmWebhook(String webhookBody) throws Exception {
        PayOS payOS = getPayOS();
        return payOS.confirmWebhook(webhookBody);
    }

    public vn.payos.type.WebhookData verifyPaymentWebhookData(vn.payos.type.Webhook webhookBody) throws Exception {
        PayOS payOS = getPayOS();
        return payOS.verifyPaymentWebhookData(webhookBody);
    }
}
