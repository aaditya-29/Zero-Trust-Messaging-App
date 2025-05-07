package com.ztma.repository;

import com.ztma.model.SecureMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SecureMessageRepository extends MongoRepository<SecureMessage, String> {
	List<SecureMessage> findByReceiver(String receiver);

	List<SecureMessage> findBySenderAndReceiver(String sender, String receiver); // <-- Add this

}
