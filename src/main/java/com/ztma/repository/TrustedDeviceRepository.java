package com.ztma.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ztma.model.TrustedDevice;

public interface TrustedDeviceRepository extends MongoRepository<TrustedDevice, String> {
    Optional<TrustedDevice> findByUserEmailAndIpAddressAndUserAgent(String userEmail, String ipAddress, String userAgent);
    List<TrustedDevice> findByUserEmail(String userEmail);
}
