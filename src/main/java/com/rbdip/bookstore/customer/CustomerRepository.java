package com.rbdip.bookstore.customer;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
            SELECT c FROM Customer c
            WHERE c.fullName = :fullName
              AND (c.address = :address OR (c.address IS NULL AND :address IS NULL))
              AND (c.phone = :phone OR (c.phone IS NULL AND :phone IS NULL))
            """)
    Optional<Customer> findMatching(
            @Param("fullName") String fullName,
            @Param("address") String address,
            @Param("phone") String phone);
}
