package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(
        origins = "https://salessavvy-frontend-latest.onrender.com",
        allowCredentials = "true"
)
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Fetches all successful orders for the authenticated user.
     *
     * @param request HttpServletRequest containing the authenticated user details.
     * @return A ResponseEntity containing the user's role, username, and their orders.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersForUser(
            HttpServletRequest request) {

        try {
            User authenticatedUser =
                    (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                return ResponseEntity
                        .status(401)
                        .body(Map.of("error", "User not authenticated"));
            }

            Map<String, Object> response =
                    orderService.getOrdersForUser(authenticatedUser);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(400)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}