package com.ztma.repository;

import com.ztma.model.UserActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserActivityLogRepository extends MongoRepository<UserActivityLog, String> {
    List<UserActivityLog> findByUserEmailAndActionTypeOrderByTimestampDesc(String email, String actionType);
}
