package com.ztma.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ztma.model.FileTransfer;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FileTransferRepository extends MongoRepository<FileTransfer, String> {
    List<FileTransfer> findByReceiver(String receiver);
    FileTransfer findByFileId(String fileId);
}
