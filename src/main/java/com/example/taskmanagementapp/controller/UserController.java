package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.UserDTO;
import com.example.taskmanagementapp.dto.mappers.UserMapper;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> findAll() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOS = UserMapper.USER_MAPPER.usersToUserDTOs(users);
        return ResponseEntity.ok(userDTOS);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable(name = "id") Long id) {

        User user = userService.findById(id);
        UserDTO userDTO = UserMapper.USER_MAPPER.userToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/workspace/{id}")
    public ResponseEntity<List<UserDTO>> findUsersByWorkspaceId(@PathVariable("id") Long id) {
        List<User> users = userService.findUsersByWorkspacesId(id);
        List<UserDTO> userDTOS = UserMapper.USER_MAPPER.usersToUserDTOs(users);
        return ResponseEntity.ok(userDTOS);
    }

    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'READ')")
    @GetMapping("/task/{id}")
    public ResponseEntity<List<UserDTO>> findUsersByTaskId(@PathVariable("id") Long id) {
        List<User> users = userService.findUsersByTasksId(id);
        List<UserDTO> userDTOS = UserMapper.USER_MAPPER.usersToUserDTOs(users);
        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> findByEmail(@PathVariable("email") String email) {
        User user = userService.findByEmail(email);
        UserDTO userDTO = UserMapper.USER_MAPPER.userToUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable("id") Long id, @RequestBody User user) {
        User user1 = userService.update(user, id);
        UserDTO userDTO = UserMapper.USER_MAPPER.userToUserDTO(user1);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDTO> deleteById(@PathVariable("id") Long id) {

        User user = userService.deleteById(id);
        UserDTO userDTO = UserMapper.USER_MAPPER.userToUserDTO(user);

        return ResponseEntity.ok(userDTO);
    }


}
