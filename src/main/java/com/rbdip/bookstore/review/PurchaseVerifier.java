package com.rbdip.bookstore.review;

public interface PurchaseVerifier {

    boolean hasPurchasedProduct(Long productId, String authorName);
}
