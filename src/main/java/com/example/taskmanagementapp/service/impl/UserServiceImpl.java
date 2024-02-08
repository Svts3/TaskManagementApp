package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.UserMapper;
import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User entity) {
        entity.setCreationDate(new Date());
        return userRepository.save(entity);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long aLong)  {
        return userRepository.findById(aLong).orElseThrow(
                ()->new UserNotFoundException(String.format("User with %d ID was not found!", aLong)));
    }

    @Transactional
    @Override
    public User update(User entity, Long aLong) {
        User user = findById(aLong);
        UserMapper.USER_MAPPER.updateUser(entity, user);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User deleteById(Long aLong) {
        User user = findById(aLong);
        userRepository.deleteById(aLong);
        return user;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                ()->new UserNotFoundException(String.format("User with %s email was not found!", email)));
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findUsersByWorkspacesId(Long id) {
        return userRepository.findUsersByWorkspacesId(id);
    }

    @Override
    public List<User> findUsersByTasksId(Long id) {
        return userRepository.findUsersByTasksId(id);
    }

    @Override
    public Boolean existsByEmailAndWorkspacesId(String email, Long workspaceId) {
        return userRepository.existsByEmailAndWorkspacesId(email, workspaceId);
    }
}
