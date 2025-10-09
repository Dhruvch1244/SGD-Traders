package com.example.demo.repository;

import com.example.demo.models.ClientIdentification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface ClientIdentificationRepository {

    long countByValueIn(@Param("values") List<String> values);

    List<ClientIdentification> findByValueIn(@Param("values") List<String> values);

    Set<ClientIdentification> findByClientId(String clientId);

    Optional<ClientIdentification> findByClientIdAndType(@Param("clientId") String clientId, @Param("type") String type);

    void insert(ClientIdentification identification);

    void updateValue(@Param("id") String id, @Param("value") String value);
}
