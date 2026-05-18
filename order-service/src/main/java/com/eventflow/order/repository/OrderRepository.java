package com.eventflow.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.eventflow.order.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
 
    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatus(String customerId, String status);
}

