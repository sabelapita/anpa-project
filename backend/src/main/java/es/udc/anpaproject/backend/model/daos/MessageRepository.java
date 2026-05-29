package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.Message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Slice<Message> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    Slice<Message> findByOrderBySentAtDesc(Pageable pageable);

    Slice<Message> findBySubjectNotOrderBySentAtDesc(String subject, Pageable pageable);
}
