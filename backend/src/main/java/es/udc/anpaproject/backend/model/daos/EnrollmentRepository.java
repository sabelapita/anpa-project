package es.udc.anpaproject.backend.model.daos;

import es.udc.anpaproject.backend.model.entities.Enrollment;
import es.udc.anpaproject.backend.model.entities.User;
import org.springframework.transaction.annotation.Transactional;
import es.udc.anpaproject.backend.model.entities.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUser(User user);
    boolean existsByUserAndActivity(User user, Activity activity);
    List<Enrollment> findByActivity(Activity activity);
    Optional<Enrollment> findByUserIdAndActivityId(Long userId, Long activityId);
    @Transactional
    void deleteByActivityId(Long activityId);

}
