package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.BlogPostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostAttachmentRepository extends JpaRepository<BlogPostAttachment, Long> {}
