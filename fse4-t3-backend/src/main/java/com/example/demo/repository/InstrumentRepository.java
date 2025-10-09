package com.example.demo.repository;

import com.example.demo.models.Instrument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface InstrumentRepository {

    List<Instrument> findAll();

    Optional<Instrument> findById(String id);

    List<Instrument> findByCriteria(@Param("description") String description, @Param("category") String category);
}
