package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/token-info")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TokenInfoController {

    /**
     * Get information about the current token including permissions
     * @return Map containing token information
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", currentUser.getId());
        tokenInfo.put("email", currentUser.getEmail());
        tokenInfo.put("fullName", currentUser.getFirstName() + " " + currentUser.getLastName());

        // Get permissions from authentication details
        if (authentication.getDetails() instanceof Map) {
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
            if (details.containsKey("permissions")) {
                tokenInfo.put("permissions", details.get("permissions"));
            }
        }

        return ResponseEntity.ok(tokenInfo);
    }
}
