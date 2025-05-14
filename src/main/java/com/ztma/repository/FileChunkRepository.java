package com.ztma.repository;

import com.ztma.model.FileChunk;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FileChunkRepository extends MongoRepository<FileChunk, String> {
    List<FileChunk> findByFileIdOrderByChunkIndexAsc(String fileId);
    long countByFileId(String fileId);
}
