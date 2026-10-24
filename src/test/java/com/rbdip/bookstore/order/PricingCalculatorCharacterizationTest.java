package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorCharacterizationTest {

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void appliesBulkDiscountOnlyAboveTenItems() {
        assertThat(total("10.00", 10, "regular", null)).isEqualByComparingTo("100.00");
        assertThat(total("10.00", 11, "regular", null)).isEqualByComparingTo("104.50");
    }

    @Test
    void appliesCustomerDiscounts() {
        assertThat(total("100.00", 1, "vip", null)).isEqualByComparingTo("90.00");
        assertThat(total("100.00", 1, "wholesale", null)).isEqualByComparingTo("85.00");
        assertThat(total("100.00", 1, "regular", null)).isEqualByComparingTo("100.00");
    }

    @Test
    void appliesCouponsInOrder() {
        assertThat(total("100.00", 1, "regular", "SAVE10")).isEqualByComparingTo("90.00");
        assertThat(total("100.00", 1, "regular", "SAVE20PERCENT")).isEqualByComparingTo("80.00");
    }

    @Test
    void doesNotReturnNegativeTotalAfterFixedCoupon() {
        assertThat(total("5.00", 1, "regular", "SAVE10")).isEqualByComparingTo("0.00");
    }

    @Test
    void appliesLargeOrderDiscountAboveThreshold() {
        assertThat(total("1000.00", 1, "regular", null)).isEqualByComparingTo("1000.00");
        assertThat(total("1001.00", 1, "regular", null)).isEqualByComparingTo("980.98");
    }

    @Test
    void roundsTheFinalTotalToTwoDecimalPlaces() {
        assertThat(total("10.005", 1, "regular", null)).isEqualByComparingTo("10.01");
    }

    private BigDecimal total(String price, int quantity, String customerType, String couponCode) {
        return calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal(price), quantity)),
                customerType,
                couponCode);
    }
}
