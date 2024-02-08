package com.example.taskmanagementapp.service;

import java.util.List;

public interface GeneralService<T, ID>{

    T save(T entity);

    List<T> findAll();

    T findById(ID id);

    T update(T entity, ID id);

    T deleteById(ID id);
}
