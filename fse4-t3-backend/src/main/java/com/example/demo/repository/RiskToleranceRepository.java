package com.example.demo.repository;

import com.example.demo.models.RiskTolerance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RiskToleranceRepository {
    List<RiskTolerance> findAll();

    Long findByName(String name);
}