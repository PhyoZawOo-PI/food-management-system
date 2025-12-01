package com.phyo.food_management_system.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.phyo.food_management_system.exception.OrderNotFoundException;
import com.phyo.food_management_system.model.Order;
import com.phyo.food_management_system.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public Order placeOrder(Order order) {
        dynamoDBMapper.save(order);
        return order;
    }

    public Optional<Order> getOrderByOrderId(String orderId) {
        return Optional.ofNullable(dynamoDBMapper.load(Order.class, orderId));
    }

    public List<Order> getOrdersByUserId(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<Order> query = new DynamoDBQueryExpression<Order>()
                .withIndexName("userId_index")
                .withConsistentRead(false)
                .withKeyConditionExpression("userId = :userId")
                .withExpressionAttributeValues(eav);

        return dynamoDBMapper.query(Order.class, query);
    }

    public List<Order> getAllOrders() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return dynamoDBMapper.scan(Order.class, scanExpression);
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {

        Order existingOrder = dynamoDBMapper.load(Order.class, orderId);

        if (existingOrder == null) {
            throw new OrderNotFoundException(orderId);
        }
        existingOrder.setUpdatedAt(LocalDateTime.now());
        existingOrder.setStatus(newStatus);   // e.g. PLACED, PREPARING, DELIVERED, CANCELLED
        dynamoDBMapper.save(existingOrder);

        return existingOrder;
    }

    public List<Order> findStuckOrders(LocalDateTime cutoffTime) {

        Map<String, String> expressionAttributeNames = Map.of(
                "#st", "status",
                "#ca", "created_at"
        );

        Map<String, AttributeValue> expressionAttributeValues = Map.of(
                ":status", new AttributeValue().withS(OrderStatus.PREPARING.name()),
                ":cutoff", new AttributeValue().withS(cutoffTime.toString())
        );

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#st = :status AND #ca <= :cutoff")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.scan(Order.class, scanExpression);
    }


}

