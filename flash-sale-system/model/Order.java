package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an order placed by a user
 * Contains order status and timestamp information
 */
public class Order {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final LocalDateTime timestamp;
    private final OrderStatus status;

    public enum OrderStatus {
        SUCCESS,
        FAILED
    }

    public Order(String orderId, String userId, String productId, OrderStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public OrderStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return String.format("Order{orderId='%s', userId='%s', productId='%s', status=%s, timestamp=%s}",
                orderId, userId, productId, status, timestamp.format(formatter));
    }
}