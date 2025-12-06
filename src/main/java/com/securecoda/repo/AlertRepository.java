package com.securecoda.repo;
import com.securecoda.model.AlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    List<AlertEntity> findAll();
    List<AlertEntity> findByResolvedFalse();
    boolean existsByDocIdAndRowIdAndDetails(String docId, String rowId, String details);

    Page<AlertEntity> findByResolvedFalse(Pageable pageable);
}
