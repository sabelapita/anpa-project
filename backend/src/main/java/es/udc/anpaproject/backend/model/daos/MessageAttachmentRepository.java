package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
}
