package com.phyo.food_management_system.service.impl;

import com.phyo.food_management_system.dto.request.OrderRequestDto;
import com.phyo.food_management_system.dto.response.OrderItemResponseDto;
import com.phyo.food_management_system.dto.response.OrderResponseDto;
import com.phyo.food_management_system.exception.MenuNotFoundException;
import com.phyo.food_management_system.exception.OrderNotFoundException;
import com.phyo.food_management_system.exception.RestaurantNotFoundException;
import com.phyo.food_management_system.model.*;
import com.phyo.food_management_system.repository.MenuRepository;
import com.phyo.food_management_system.repository.OrderRepository;
import com.phyo.food_management_system.repository.RestaurantRepository;
import com.phyo.food_management_system.repository.UserRepository;
import com.phyo.food_management_system.security.CustomUserDetails;
import com.phyo.food_management_system.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @CachePut(value = "orders", key = "#result.orderId")
    public OrderResponseDto placeOrder(OrderRequestDto orderRequestDto) {

        // Get logged-in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String userId = userDetails.getUserId();

        // Convert DTO → Order entity
        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(orderRequestDto.getRestaurantId());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PLACED);

        // Calculate total price
        double total = 0.0;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequestDto.OrderItemRequestDto itemDto : orderRequestDto.getItems()) {

            Menu menuItem = menuRepository.getMenuItemById(itemDto.getMenuItemId()).orElseThrow(() -> new MenuNotFoundException(itemDto.getMenuItemId()));


            double price = menuItem.getPrice();               // price at time of order
            double itemTotal = price * itemDto.getQuantity(); // quantity * price

            // Build OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemDto.getMenuItemId());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(price);
            orderItem.setTotalPrice(itemTotal);

            orderItems.add(orderItem);

            total += itemTotal;
        }

        // Add items and total price to order
        order.setItems(orderItems);
        order.setTotalPrice(total);

        // Save to DynamoDB
        Order savedOrder = orderRepository.placeOrder(order);
        return toOrderResponseDto(savedOrder);

    }


    // Get order by orderId (USER/ADMIN)
    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponseDto getOrderById(String orderId) {
        log.info("Fetching order from database (not cache)");
        Order order = orderRepository.getOrderByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return toOrderResponseDto(order);
    }


    // List orders by userId (ADMIN)
    @Override
    @Cacheable(value = "orders", key = "#userId")
    public List<OrderResponseDto> getOrdersByUserId(String userId) {
        log.info("Fetching orders from database (not cache)");
        List<Order> orders = orderRepository.getOrdersByUserId(userId);

        return orders.stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());
    }

    // List all orders (ADMIN)
    @Override
    @Cacheable(value = "orders")
    public List<OrderResponseDto> getAllOrders() {

        List<Order> orders = orderRepository.getAllOrders();

        return orders.stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());
    }


    // Update order status (USER/ADMIN)
    @Override
    @CachePut(value = "orders", key = "#result.orderId")
    public OrderResponseDto updateOrderStatus(String orderId, OrderStatus newStatus) {

        // Ensure the order exists
        orderRepository.getOrderByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Update and return
        Order updatedOrder = orderRepository.updateOrderStatus(orderId, newStatus);

        return toOrderResponseDto(updatedOrder);
    }

    @Override
    @CachePut(value = "orders", key = "#orderId")
    public OrderResponseDto cancelOrder(String orderId) {
        Order order = orderRepository.getOrderByOrderId(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel this order");
        }

        Order updatedOrder = orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);

        return toOrderResponseDto(updatedOrder);
    }

    @Override
    public List<Order> getStuckOrders(LocalDateTime cutoffTime) {
        // Delegate to repository method
        return orderRepository.findStuckOrders(cutoffTime);
    }


    private OrderResponseDto toOrderResponseDto(Order order) {

        User user = userRepository.getUserById(order.getUserId()).orElseThrow(() -> new UsernameNotFoundException(order.getUserId()));
        Restaurant restaurant = restaurantRepository.getRestaurantById(order.getRestaurantId()).orElseThrow(() -> new RestaurantNotFoundException(order.getRestaurantId()));

        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setRestaurantId(order.getRestaurantId());
        dto.setUserName(user.getUsername());
        dto.setRestaurantName(restaurant.getName());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Convert list of items
        List<OrderItemResponseDto> itemDtos = order.getItems().stream()
                .map(this::toOrderItemResponseDto)
                .toList();

        dto.setItems(itemDtos);

        return dto;
    }

    private OrderItemResponseDto toOrderItemResponseDto(OrderItem item) {

        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setMenuItemId(item.getMenuItemId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotalPrice(item.getTotalPrice());

        return dto;
    }

}

