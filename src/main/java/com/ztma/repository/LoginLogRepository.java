package com.ztma.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ztma.model.LoginLog;

public interface LoginLogRepository extends MongoRepository<LoginLog, String> {
    List<LoginLog> findByEmail(String email);
}
