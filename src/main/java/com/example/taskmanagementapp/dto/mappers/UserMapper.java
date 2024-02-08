package com.example.taskmanagementapp.dto.mappers;

import com.example.taskmanagementapp.dto.UserDTO;
import com.example.taskmanagementapp.model.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface UserMapper {

    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    UserDTO userToUserDTO(User user);

    List<UserDTO>usersToUserDTOs(List<User>user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorities", expression = "java(user.getAuthorities())", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(User updatedUser, @MappingTarget User user);
}
