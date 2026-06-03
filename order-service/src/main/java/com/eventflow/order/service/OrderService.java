package com.eventflow.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.eventflow.order.dto.CreateOrderRequest;
import com.eventflow.order.model.Order;
import com.eventflow.order.model.OrderItem;
import com.eventflow.order.model.OrderStatus;
import com.eventflow.order.repository.OrderRepository;
import com.eventflow.order.event.OrderCreatedEvent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        // Step 1 - Build Order entity from request
        Order order = Order.builder()
            .customerId(request.getCustomerId())
            .status(OrderStatus.PENDING)
            .totalAmount(request.getItems().stream()
                .map(i -> i.getUnitPrice()
                    .multiply(new java.math.BigDecimal(i.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))
            .build();

        // Step 2 - Build OrderItems and link to Order
        List<OrderItem> items = request.getItems().stream()
            .map(i -> OrderItem.builder()
                .order(order)
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .order(order)
                .build())
            .collect(Collectors.toList());

        order.setItems(items);

        // Step 3 - Save to PostgreSQL
        Order savedOrder =  orderRepository.save(order);
        log.info("Order saved to DB: {}", savedOrder.getId());

        // Step 4 - Build Kafka event
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(savedOrder.getId())
            .customerId(savedOrder.getCustomerId())
            .totalAmount(savedOrder.getTotalAmount())
            .status(savedOrder.getStatus().name())
            .createdAt(LocalDateTime.now())
            .items(request.getItems().stream()
                .map(i -> OrderCreatedEvent.OrderItem.builder()
                    .productId(i.getProductId())
                    .quantity(i.getQuantity())
                    .unitPrice(i.getUnitPrice())
                    .build())
                .collect(Collectors.toList()))
            .build();
        
        // Step 5 - Publish to Kafka
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, savedOrder.getId().toString(), event);
        log.info("Order event published to Kafka topic: {}", ORDER_EVENTS_TOPIC);
    
        return savedOrder;
    }

        public Order getOrder(UUID orderId) {
            return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        }

        public List<Order> getOrdersByCustomerId(String customerId) {
            return orderRepository.findByCustomerId(customerId);
        }
    }

