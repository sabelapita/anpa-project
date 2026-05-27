package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.BlogPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    //Para users: solo posts publicados
    Slice<BlogPost> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Optional<BlogPost> findByIdAndPublishedTrue(Long id);

    Slice<BlogPost> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT p FROM BlogPost p
        WHERE p.published = true
        AND (:keywords IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keywords, '%')))
        AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
        ORDER BY p.createdAt DESC
    """)
    Slice<BlogPost> findPublishedFiltered(
            @Param("keywords") String keywords,
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM BlogPost p
        WHERE (:keywords IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keywords, '%')))
        AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
        ORDER BY p.createdAt DESC
    """)
    Slice<BlogPost> findAllFiltered(
            @Param("keywords") String keywords,
            @Param("category") String category,
            Pageable pageable
    );
}
