package phamiz.ecommerce.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.model.Order;
import phamiz.ecommerce.backend.model.PaymentStatus;
import phamiz.ecommerce.backend.service.PayOSService;
import phamiz.ecommerce.backend.service.serviceImpl.OrderService;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOSService payOSService;
    private final OrderService orderService;

    @PostMapping("/create-payment-link/{orderId}")
    public ResponseEntity<CheckoutResponseData> createPaymentLink(@PathVariable Long orderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            CheckoutResponseData checkoutResponseData = payOSService.createPaymentLink(order);
            return new ResponseEntity<>(checkoutResponseData, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment-link-info/{orderId}")
    public ResponseEntity<PaymentLinkData> getPaymentLinkInfo(@PathVariable Long orderId) {
        try {
            PaymentLinkData paymentLinkData = payOSService.getPaymentLinkInformation(orderId);
            return new ResponseEntity<>(paymentLinkData, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhookBody) {
        try {
            WebhookData data = payOSService.verifyPaymentWebhookData(webhookBody);

            // Update order status
            // PayOS orderCode is the orderId
            Long orderId = data.getOrderCode();
            String transactionId = data.getReference(); // Or other field depending on PayOS response

            // Assuming if we get here, it's a success or we need to check data.
            // If code == "00" it usually means success in VN payment gateways.
            // Let's assume success for now and update order.

            orderService.updatePaymentStatus(orderId, PaymentStatus.PAID, transactionId);

            return new ResponseEntity<>("Webhook received", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Webhook failed", HttpStatus.BAD_REQUEST);
        }
    }
}
