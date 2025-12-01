package com.phyo.food_management_system.controller;

import com.phyo.food_management_system.dto.request.OrderRequestDto;
import com.phyo.food_management_system.dto.response.ErrorResponseDto;
import com.phyo.food_management_system.dto.response.OrderResponseDto;
import com.phyo.food_management_system.exception.UserNotFoundException;
import com.phyo.food_management_system.model.Order;
import com.phyo.food_management_system.model.OrderStatus;
import com.phyo.food_management_system.model.User;
import com.phyo.food_management_system.repository.UserRepository;
import com.phyo.food_management_system.security.CustomUserDetails;
import com.phyo.food_management_system.service.EmailService;
import com.phyo.food_management_system.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.List;

@Tag(
        name = "Orders",
        description = "APIs for placing orders, updating order status, and retrieving order information"
)
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Place a new order",
            description = "Allows a USER to place a new order. Requires a valid JWT token. An email notification will be sent asynchronously after the order is placed.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order details to place",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderRequestDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order placed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – only USER role can place orders",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // Place an order - USER only
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto order = orderService.placeOrder(orderRequestDto);
        // Send email asynchronously
        sendOrderNotificationEmail(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(order);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Allows a USER to view their own order and an ADMIN to view any order. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "orderId",
                            description = "ID of the order to retrieve",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order details retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – USER can access only their own orders; ADMIN can access any order",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    //User can see own order and Admin can any
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderByOrderId(
            @PathVariable String orderId,
            Authentication authentication) {

        OrderResponseDto order = orderService.getOrderById(orderId);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
            if (!order.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("You cannot access this order");
            }
        }

        return ResponseEntity.ok(order);
    }


    @Operation(
            summary = "Get orders of a specific user",
            description = "Allows an ADMIN to view all orders for a given user. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "userId",
                            description = "ID of the user whose orders are being retrieved",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of orders retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = OrderResponseDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – only ADMIN users can access this resource",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // List orders of a specific user - ADMIN only
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUser(@PathVariable String userId) {
        List<OrderResponseDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get all orders",
            description = "Allows an ADMIN to view all orders in the system. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of all orders retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = OrderResponseDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – only ADMIN users can access this resource",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // List all orders - ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Update order status",
            description = "Allows a USER to update their own order status and an ADMIN to update any order. Requires a valid JWT token.",
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            },
            parameters = {
                    @Parameter(
                            name = "orderId",
                            description = "ID of the order to update",
                            required = true,
                            in = ParameterIn.PATH
                    ),
                    @Parameter(
                            name = "status",
                            description = "New status for the order",
                            required = true,
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = OrderStatus.class)
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order status updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – missing or invalid JWT token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – only ADMIN or the owner of the order can update this order",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
                    )
            }
    )
    // User can update own oder and Admin can any
    @PatchMapping("/{orderId}/status")
   public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status, Authentication authentication) {
        OrderResponseDto order = orderService.getOrderById(orderId);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();
            if (!order.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("You cannot access this order");
            }
        }

        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, status);
        // Send email asynchronously
        sendOrderNotificationEmail(updatedOrder);
        return ResponseEntity.ok(updatedOrder);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // every 5 minutes
    @Async("asyncTaskExecutor")
    public void autoCancelStuckOrders() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<Order> stuckOrders = orderService.getStuckOrders(cutoffTime);

        stuckOrders.forEach(order -> {
            try {
                orderService.cancelOrder(order.getOrderId());
                log.info("Cancelled order: {} by {} " , order.getOrderId(), Thread.currentThread().getName());
            } catch (Exception e) {
                log.error("Failed to cancel order {} : {} " , order.getOrderId() , e.getMessage());
            }
        });
    }

    private void sendOrderNotificationEmail(OrderResponseDto order) {
        // Fetch user email
        User user = userRepository.getUserById(order.getUserId())
                .orElseThrow(() -> new UserNotFoundException(order.getUserId()));

        String email = user.getEmail();
        String subject = "Order Update - " + order.getOrderId();
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(user.getUsername()).append(",\n\n")
                .append("Your order details:\n")
                .append("Order ID: ").append(order.getOrderId()).append("\n")
                .append("Status: ").append(order.getStatus()).append("\n")
                .append("Total: ").append(order.getTotalPrice()).append("\n\n")
                .append("Thank you for ordering with us!");
        String emailBody = body.toString();
        emailService.sendEmail(email, subject, emailBody);
    }
}
