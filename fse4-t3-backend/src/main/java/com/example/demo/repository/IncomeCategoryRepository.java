package com.example.demo.repository;

import com.example.demo.models.IncomeCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IncomeCategoryRepository {
    List<IncomeCategory> findAll();

    Long findByRange(String range);

}
