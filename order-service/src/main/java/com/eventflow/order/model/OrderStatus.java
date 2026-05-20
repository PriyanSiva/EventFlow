package com.eventflow.order.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_PROCESSING,
    PAYMENT_FAILED,
    FULFILLED,
    CANCELLED,
}
