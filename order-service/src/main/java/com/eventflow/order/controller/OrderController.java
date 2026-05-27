package com.eventflow.order.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventflow.order.dto.CreateOrderRequest;
import com.eventflow.order.model.Order;
import com.eventflow.order.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Received order request for customer: {}", request.getCustomerId());
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderID")
    public ResponseEntity<Order> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
    
    
    
}
