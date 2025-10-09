package com.example.demo.repository;

import com.example.demo.models.Price;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PriceRepository {

    List<Price> findAll();

    List<Price> findLatestPrices();

    Optional<Price> findLatestPriceForInstrument(String instrumentId);
}
