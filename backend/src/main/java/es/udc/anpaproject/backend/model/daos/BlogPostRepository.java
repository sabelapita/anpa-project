package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.BlogPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    //Para users: solo posts publicados
    Slice<BlogPost> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Optional<BlogPost> findByIdAndPublishedTrue(Long id);

    Slice<BlogPost> findByOrderByCreatedAtDesc(Pageable pageable);
}
