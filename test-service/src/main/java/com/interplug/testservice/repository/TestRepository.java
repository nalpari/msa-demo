package com.interplug.testservice.repository;

import com.interplug.testservice.entity.Test;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TestRepository extends R2dbcRepository<Test, Long> {

    Flux<Test> findByNameContaining(String keyword);
}