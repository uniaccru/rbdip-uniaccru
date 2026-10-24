package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    public void sendConfirmation(String customerName, Long orderId, BigDecimal total) {
        // Реальный почтовый транспорт не настроен в учебном проекте.
        System.out.printf(
                "[email] Dear %s, your order #%d for %s has been placed.%n", customerName, orderId, total);
    }
}
