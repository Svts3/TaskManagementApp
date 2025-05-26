package com.example.taskmanagementapp.dto;

import com.example.taskmanagementapp.dto.mappers.UserMapper;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserMapperTest {


    private User user;

    private List<User> users;

    @BeforeEach
    void setUp(){
        users = new ArrayList<>();
        Task task = Task.builder().id(1L).title("title").content("content")
                .creationDate(new Date()).build();
        user = User.builder().id(1L).email("email").firstName("firstName").lastName("lastName")
                .creationDate(new Date()).tasks(List.of(task)).build();

        task.setCreator(user);
        task.setPerformers(List.of(user));
        users.add(user);
    }

    @Test
    void testUserMapper(){

        UserDTO userDTO = UserMapper.USER_MAPPER.userToUserDTO(user);
        assertNotNull(userDTO);
        assertNotNull(userDTO.getCreationDate());

        assertEquals(1L, userDTO.getId());
        assertEquals("email", userDTO.getEmail());
        assertEquals("firstName", userDTO.getFirstName());
        assertEquals("lastName", userDTO.getLastName());
    }

    @Test
    void testUserMapperList(){
        users.add(User.builder().id(2L).firstName("firstName2").lastName("lastName2").email("email2").build());
        List<UserDTO>userDTOS = UserMapper.USER_MAPPER.usersToUserDTOs(users);

        assertEquals(2, userDTOS.size());

        UserDTO user1 = userDTOS.get(0);
        assertNotNull(user1);
        assertNotNull(user1.getCreationDate());

        assertEquals(1L, user1.getId());
        assertEquals("email", user1.getEmail());
        assertEquals("firstName", user1.getFirstName());
        assertEquals("lastName", user1.getLastName());

        UserDTO user2 = userDTOS.get(1);
        assertNotNull(user2);

        assertEquals(2L, user2.getId());
        assertEquals("email2", user2.getEmail());
        assertEquals("firstName2", user2.getFirstName());
        assertEquals("lastName2", user2.getLastName());
    }

}
