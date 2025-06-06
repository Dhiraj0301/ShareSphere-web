package com.example.ShareSphere.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ShareSphere.entity.FileEntity; // Ensure this path matches the actual location of FileEntity

@Repository
public interface FileRepository  extends JpaRepository<FileEntity,Integer> {
    // This interface extends JpaRepository, which provides CRUD operations for the FileEntity class.
    // The first parameter is the entity type, and the second parameter is the type of the entity's ID.
    // No additional methods are defined here, as JpaRepository already provides common database operations.

    List<FileEntity> findByExpiryTimeBefore(LocalDateTime now); // Custom query method to find files that have expired.
    // This method will be automatically implemented by Spring Data JPA based on the method name.

    FileEntity findByFileName(String fileName);
    // Optional: This method is actually already provided by JpaRepository
    Optional<FileEntity> findById(Integer id);

} 