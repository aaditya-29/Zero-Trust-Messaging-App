package com.ztma.repository;

import com.ztma.model.UserActivityLog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface UserActivityLogRepository extends MongoRepository<UserActivityLog, String> {

    List<UserActivityLog> findByTimestampAfter(Date cutoff);

    List<UserActivityLog> findByUserEmail(String email);

    @Query("{ 'timestamp' : { $gte: ?0 } }")
    List<UserActivityLog> findRecentLogs(Date cutoff);
}
