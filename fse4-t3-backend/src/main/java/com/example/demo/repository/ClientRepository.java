package com.example.demo.repository;

import com.example.demo.models.Client;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface ClientRepository {

    Optional<Client> findByEmail(String email);

    Optional<Client> findById(String clientId);

    void insert(Client client);

    void update(Client client);

    long count();

    void deleteById(String clientId);
}
