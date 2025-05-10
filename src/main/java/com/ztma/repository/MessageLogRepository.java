package com.ztma.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ztma.model.MessageLog;

public interface MessageLogRepository extends MongoRepository<MessageLog, String> {
    List<MessageLog> findBySenderAndTimestampAfter(String sender, long after);
}

