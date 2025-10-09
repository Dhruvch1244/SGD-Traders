package com.example.demo.repository;

import com.example.demo.models.InvestmentPurpose;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvestmentPurposeRepository {
    List<InvestmentPurpose> findAll();

    Long findByPurpose(String purpose);
}
