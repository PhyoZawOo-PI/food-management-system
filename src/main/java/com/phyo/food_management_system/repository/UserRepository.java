package com.phyo.food_management_system.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.phyo.food_management_system.exception.UserNotFoundException;
import com.phyo.food_management_system.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public User register(User user){
        dynamoDBMapper.save(user);
        return user;
    }

    public Optional<User> getUserById(String userId) {
        return Optional.ofNullable(dynamoDBMapper.load(User.class, userId));
    }

    public List<User> getAllUsers() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return dynamoDBMapper.scan(User.class, scanExpression);
    }

    public User updateUser(String userId, User updatedUser) {
        User existingUser = dynamoDBMapper.load(User.class, userId);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            dynamoDBMapper.save(existingUser);
            return existingUser;
        } else {
            throw new UserNotFoundException(updatedUser.getEmail());
        }
    }

    public void deleteUser(String userId) {
        User user = dynamoDBMapper.load(User.class, userId);
        if (user != null) {
            dynamoDBMapper.delete(user);
        } else {
            throw new UserNotFoundException("User not found with the given id : " + userId);
        }
    }

    public Optional<User> findByEmail(String email) {
        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("email_index")  // Use the GSI
                .withConsistentRead(false)     // Must be false for GSI
                .withKeyConditionExpression("email = :emailVal")
                .withExpressionAttributeValues(Map.of(":emailVal", new AttributeValue().withS(email)));

        List<User> user = dynamoDBMapper.query(User.class, queryExpression);

        return Optional.ofNullable(user.isEmpty() ? null : user.get(0));
    }
}
