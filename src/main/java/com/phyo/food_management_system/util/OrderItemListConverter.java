package com.phyo.food_management_system.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phyo.food_management_system.model.OrderItem;

import java.util.List;

public class OrderItemListConverter implements DynamoDBTypeConverter<String, List<OrderItem>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(List<OrderItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert OrderItem list to JSON", e);
        }
    }

    @Override
    public List<OrderItem> unconvert(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<OrderItem>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON to OrderItem list", e);
        }
    }
}

