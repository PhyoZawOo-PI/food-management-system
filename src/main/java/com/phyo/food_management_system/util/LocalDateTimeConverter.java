package com.phyo.food_management_system.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.time.LocalDateTime;

public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {

    @Override
    public String convert(LocalDateTime time) {
        return time.toString();  // ISO-8601 format
    }

    @Override
    public LocalDateTime unconvert(String time) {
        return LocalDateTime.parse(time);
    }
}

