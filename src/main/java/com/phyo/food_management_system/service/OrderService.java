package com.phyo.food_management_system.service;

import com.phyo.food_management_system.dto.request.OrderRequestDto;
import com.phyo.food_management_system.dto.response.OrderResponseDto;
import com.phyo.food_management_system.model.Order;
import com.phyo.food_management_system.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    // Place an order (USER only)
    OrderResponseDto placeOrder(OrderRequestDto orderRequestDto);

    // Get order details by orderId (USER/ADMIN)
    OrderResponseDto getOrderById(String orderId);

    // List orders of a specific user (ADMIN only)
    List<OrderResponseDto> getOrdersByUserId(String userId);

    // List all orders (ADMIN)
    List<OrderResponseDto> getAllOrders();

    // Update order status (USER/ADMIN)
    OrderResponseDto updateOrderStatus(String orderId, OrderStatus newStatus);

    // Cancel Order
    OrderResponseDto cancelOrder(String orderId);

    //Get all stuck orders
    List<Order> getStuckOrders(LocalDateTime cutoffTime);

}