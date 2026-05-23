package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.ActivityVisibility;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>{
    Slice<Activity> findByOrderByTitleAsc(Pageable pageable);
    Slice<Activity> findByVisibilityInOrderByTitleAsc(List<ActivityVisibility> visibilities, Pageable pageable);
}
