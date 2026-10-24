package com.rbdip.bookstore.order;

import com.rbdip.bookstore.customer.Customer;
import com.rbdip.bookstore.customer.CustomerService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Координирует создание заказа, делегируя отдельные ответственности
 * специализированным компонентам.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRequestValidator requestValidator;
    private final OrderItemPreparationService itemPreparationService;
    private final CustomerService customerService;
    private final PricingCalculator pricingCalculator;
    private final OrderNotificationService notificationService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderRequestValidator requestValidator,
            OrderItemPreparationService itemPreparationService,
            CustomerService customerService,
            PricingCalculator pricingCalculator,
            OrderNotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.requestValidator = requestValidator;
        this.itemPreparationService = itemPreparationService;
        this.customerService = customerService;
        this.pricingCalculator = pricingCalculator;
        this.notificationService = notificationService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        requestValidator.validate(request);
        List<OrderItemPreparationService.PreparedItem> items = itemPreparationService.prepare(request.items());
        BigDecimal total = pricingCalculator.calculateOrderTotal(
                items.stream()
                        .map(item -> new PricingCalculator.LineItem(item.product().getPrice(), item.quantity()))
                        .toList(),
                request.customerType(),
                request.couponCode());

        Customer customer = customerService.findOrCreate(
                request.customerFullName(), request.customerAddress(), request.customerPhone());
        Order order = new Order(customer, "new");
        order = orderRepository.save(order);

        for (OrderItemPreparationService.PreparedItem item : items) {
            orderItemRepository.save(new OrderItem(
                    order.getId(), item.product(), item.quantity()));
        }

        notificationService.sendConfirmation(request.customerFullName(), order.getId(), total);
        return order;
    }
}
