package com.example.taskmanagementapp.security;

import com.example.taskmanagementapp.exception.UserIdMismatchException;
import com.example.taskmanagementapp.model.User;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class UserSecurity implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        Long userId = Long.valueOf(object.getVariables().get("id"));
        User user = (User) authentication.get().getPrincipal();
        if (!user.getId().equals(userId)) {
            throw new UserIdMismatchException(String.format("Authenticated user's id %d and requested id %d don't match",
                    user.getId(), userId));
        }
        return new AuthorizationDecision(true);
    }
}
