package com.securecoda.repo;
import com.securecoda.model.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
    Page<DocumentEntity> findAll(Pageable pageable);
}
