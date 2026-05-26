package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Slice<Document> findByOrderByUploadedAtDesc(Pageable pageable);

    Slice<Document> findByTitleContainingIgnoreCaseOrderByUploadedAtDesc(String keywords, Pageable pageable);

}
