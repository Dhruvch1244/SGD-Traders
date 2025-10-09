package com.example.demo.repository;

import com.example.demo.models.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderRepository {
    List<Order> findAll();

    List<Order> findByClientId(String clientId);

    void insert(Order order);
}
