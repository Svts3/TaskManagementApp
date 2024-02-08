package com.example.taskmanagementapp.dto.mappers;

import com.example.taskmanagementapp.dto.TaskDTO;
import com.example.taskmanagementapp.model.Task;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface TaskMapper {

    TaskMapper TASK_MAPPER = Mappers.getMapper(TaskMapper.class);

    TaskDTO taskToTaskDTO(Task task);
    List<TaskDTO> tasksToTaskDTOs(List<Task>tasks);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTask(Task updateTask, @MappingTarget Task task);
}
