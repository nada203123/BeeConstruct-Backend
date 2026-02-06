package com.dpc.chantierservice.repositories;
import com.dpc.chantierservice.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByChantierId(Long chantierId);


}
