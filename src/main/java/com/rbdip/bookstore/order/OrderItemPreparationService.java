package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderItemPreparationService {

    private static final int DEFAULT_QUANTITY = 1;

    private final ProductRepository productRepository;

    public OrderItemPreparationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<PreparedItem> prepare(List<CreateOrderRequest.Item> rawItems) {
        return rawItems.stream()
                .map(this::prepareItem)
                .toList();
    }

    private PreparedItem prepareItem(CreateOrderRequest.Item rawItem) {
        Product product = productRepository.findById(rawItem.productId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "product " + rawItem.productId() + " not found"));
        int quantity = rawItem.quantity() == null ? DEFAULT_QUANTITY : rawItem.quantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        return new PreparedItem(product, quantity);
    }

    public record PreparedItem(Product product, int quantity) {
    }
}
