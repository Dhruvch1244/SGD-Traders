package com.example.demo.repository;

import com.example.demo.models.InvestmentDuration;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvestmentDurationRepository {
    List<InvestmentDuration> findAll();

    Long findByDuration(String duration);
}