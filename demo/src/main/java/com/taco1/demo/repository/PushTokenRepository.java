package com.taco1.demo.repository;

import com.taco1.demo.entity.PushTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushTokenRepository extends JpaRepository<PushTokenEntity, String> {

    boolean existsByToken(String token); // Check if a token exists in the database

}
