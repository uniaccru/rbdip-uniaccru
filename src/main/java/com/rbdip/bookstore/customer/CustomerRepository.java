package com.rbdip.bookstore.customer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @Query("""
            SELECT c FROM Customer c
            WHERE c.firstName = :firstName
              AND c.lastName = :lastName
              AND (c.address = :address OR (c.address IS NULL AND :address IS NULL))
              AND (c.phone = :phone OR (c.phone IS NULL AND :phone IS NULL))
            """)
    Optional<Customer> findMatching(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("address") String address,
            @Param("phone") String phone);
}
